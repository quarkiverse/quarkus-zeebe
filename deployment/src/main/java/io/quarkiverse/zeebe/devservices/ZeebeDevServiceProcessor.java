package io.quarkiverse.zeebe.devservices;

import static io.quarkiverse.zeebe.ZeebeProcessor.FEATURE_NAME;
import static io.quarkus.runtime.LaunchMode.DEVELOPMENT;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import io.quarkiverse.zeebe.ZeebeDevServiceBuildTimeConfig;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.zeebe.containers.*;

public class ZeebeDevServiceProcessor {

    private static final Logger log = Logger.getLogger(ZeebeDevServiceProcessor.class);
    static final String PROP_ZEEBE_GATEWAY_ADDRESS = "quarkus.zeebe.client.broker.gateway-address";
    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-zeebe";
    public static final int DEFAULT_ZEEBE_PORT = ZeebePort.GATEWAY.getPort();
    private static final ContainerLocator zeebeContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL, DEFAULT_ZEEBE_PORT);
    static volatile ZeebeRunningDevService devService;
    static volatile ZeebeDevServiceCfg cfg;
    static volatile boolean first = true;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { GlobalDevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startZeebeContainers(LaunchModeBuildItem launchMode,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            ZeebeDevServiceBuildTimeConfig buildTimeConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            DockerStatusBuildItem dockerStatusBuildItem,
            BuildProducer<ZeebeDevServicesProviderBuildItem> startResultProducer,
            LoggingSetupBuildItem loggingSetupBuildItem, GlobalDevServicesConfig devServicesConfig) {

        ZeebeDevServiceCfg configuration = getConfiguration(buildTimeConfig);

        if (devService != null) {
            boolean shouldShutdownTheBroker = !configuration.equals(cfg);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            stopZeebe();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Zeebe Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startZeebe(dockerStatusBuildItem, configuration, launchMode,
                    !devServicesSharedNetworkBuildItem.isEmpty(),
                    devServicesConfig.timeout);
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    stopZeebe();
                }
                first = true;
                devService = null;
                cfg = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        cfg = configuration;

        if (devService.isOwner()) {
            String tmp = devService.getConfig().get(PROP_ZEEBE_GATEWAY_ADDRESS);
            log.infof("The zeebe broker is ready to accept connections on %s (http://%s)",
                    tmp, tmp);
            if (configuration.monitor) {
                startResultProducer.produce(new ZeebeDevServicesProviderBuildItem(devService.zeebeInternalUrl));
            }
        }

        return devService.toBuildItem();
    }

    public static class ZeebeRunningDevService extends RunningDevService {

        private String zeebeInternalUrl;

        public ZeebeRunningDevService(String name, String containerId, Closeable closeable, Map<String, String> config,
                String zeebeInternalUrl) {
            super(name, containerId, closeable, config);
            this.zeebeInternalUrl = zeebeInternalUrl;
        }
    }

    private ZeebeRunningDevService startZeebe(DockerStatusBuildItem dockerStatusBuildItem,
            ZeebeDevServiceCfg config,
            LaunchModeBuildItem launchMode, boolean useSharedNetwork, Optional<Duration> timeout) {

        if (!config.devServicesEnabled) {
            // explicitly disabled
            log.debug("Not starting dev services for Zeebe as it has been disabled in the config");
            return null;
        }

        if (ConfigUtils.isPropertyPresent(PROP_ZEEBE_GATEWAY_ADDRESS)) {
            log.debug("Not starting dev services for Zeebe as '" + PROP_ZEEBE_GATEWAY_ADDRESS + "' have been provided");
            return null;
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            log.warn(
                    "Docker isn't working, please configure the zeebe broker servers gateway property ("
                            + PROP_ZEEBE_GATEWAY_ADDRESS + ").");
            return null;
        }

        final Optional<ContainerAddress> maybeContainerAddress = zeebeContainerLocator.locateContainer(config.serviceName,
                config.shared,
                launchMode.getLaunchMode());

        // Starting the broker
        final Supplier<ZeebeRunningDevService> defaultZeebeBrokerSupplier = () -> {

            DockerImageName image = ZeebeDefaults.getInstance().getDefaultDockerImage();
            if (config.imageName != null) {
                image = DockerImageName.parse(config.imageName);
            }

            int testDebugExportPort = config.testDebugExportPort;
            if (launchMode.isTest() && config.testExporter) {
                if (config.testDebugExportPort == 0) {
                    try (ServerSocket serverSocket = new ServerSocket(0)) {
                        testDebugExportPort = serverSocket.getLocalPort();
                    } catch (IOException e) {
                        log.error("Port for debug exporter receiver is not available");
                    }
                }
            }

            QuarkusZeebeContainer container = new QuarkusZeebeContainer(
                    image,
                    config.fixedExposedPort,
                    launchMode.getLaunchMode() == DEVELOPMENT ? config.serviceName : null,
                    useSharedNetwork,
                    launchMode.isTest(),
                    testDebugExportPort,
                    config.monitor,
                    config.debugExporter,
                    config.debugReceiverPort);
            timeout.ifPresent(container::withStartupTimeout);
            container.start();

            String gateway = String.format("%s:%d", container.getGatewayHost(), container.getPort());
            String zeebeInternalUrl = container.getInternalAddress(DEFAULT_ZEEBE_PORT);
            String testClient = container.getExternalAddress(DEFAULT_ZEEBE_PORT);

            return new ZeebeRunningDevService(FEATURE_NAME,
                    container.getContainerId(),
                    container::close,
                    configMap(gateway, launchMode.isTest(), testClient, testDebugExportPort, config.testExporter),
                    zeebeInternalUrl);
        };

        return maybeContainerAddress
                .map(containerAddress -> new ZeebeRunningDevService(FEATURE_NAME,
                        containerAddress.getId(),
                        null, configMap(containerAddress.getUrl(), false, null, null, false), null))
                .orElseGet(defaultZeebeBrokerSupplier);
    }

    private static Map<String, String> configMap(String gateway, boolean test, String testClient, Integer testDebugExportPort,
            boolean testExporter) {
        Map<String, String> config = new HashMap<>();
        config.put(PROP_ZEEBE_GATEWAY_ADDRESS, gateway);
        if (test && testExporter) {
            if (testDebugExportPort != null) {
                config.put("quarkiverse.zeebe.devservices.test.receiver-port", "" + testDebugExportPort);
            }
            if (testClient != null) {
                config.put("quarkiverse.zeebe.devservices.test.gateway-address", testClient);
            }
        }
        return config;
    }

    private void stopZeebe() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop the Zeebe broker", e);
            } finally {
                devService = null;
            }
        }
    }

    private ZeebeDevServiceCfg getConfiguration(ZeebeDevServiceBuildTimeConfig cfg) {
        ZeebeDevServicesConfig devServicesConfig = cfg.devService;
        return new ZeebeDevServiceCfg(devServicesConfig);
    }

    private static final class ZeebeDevServiceCfg {
        private final boolean devServicesEnabled;
        private final String imageName;
        private final Integer fixedExposedPort;
        private final boolean shared;
        private final String serviceName;

        private final boolean monitor;

        private final boolean testExporter;
        private final int testDebugExportPort;

        private final boolean debugExporter;

        private final int debugReceiverPort;

        public ZeebeDevServiceCfg(ZeebeDevServicesConfig config) {
            this.devServicesEnabled = config.enabled;
            this.imageName = config.imageName.orElse(null);
            this.fixedExposedPort = config.port.orElse(0);
            this.shared = config.shared;
            this.serviceName = config.serviceName;
            this.monitor = config.monitor.enabled;
            this.testExporter = config.test.exporter;
            this.testDebugExportPort = config.test.receiverPort.orElse(0);
            this.debugExporter = config.debugExporter.enabled;
            this.debugReceiverPort = config.debugExporter.receiverPort;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ZeebeDevServiceCfg that = (ZeebeDevServiceCfg) o;
            return devServicesEnabled == that.devServicesEnabled && Objects.equals(imageName, that.imageName)
                    && Objects.equals(fixedExposedPort, that.fixedExposedPort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devServicesEnabled, imageName, fixedExposedPort);
        }
    }

    private static class QuarkusZeebeContainer extends ZeebeContainer {

        private final int fixedExposedPort;
        private final boolean useSharedNetwork;

        private String hostName = null;

        public QuarkusZeebeContainer(DockerImageName image, int fixedExposedPort, String serviceName,
                boolean useSharedNetwork, boolean test, int testDebugExportPort, boolean monitor, boolean debugExporter,
                int debugExporterPort) {
            super(image);
            log.debugf("Zeebe broker docker image %s", image);
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
            if (serviceName != null) {
                withLabel(DEV_SERVICE_LABEL, serviceName);
            }
            if (test) {
                // create random port
                withDebugExporter(testDebugExportPort);
            } else {
                if (monitor) {
                    withMonitorDebugExporter();
                } else if (debugExporter) {
                    withDebugExporter(debugExporterPort);
                }
            }
        }

        public void withMonitorDebugExporter() {
            //noinspection resource
            withCopyToContainer(MountableFile.forClasspathResource("debug-exporter.jar"), "/tmp/debug-exporter.jar")
                    .withEnv("ZEEBE_BROKER_EXPORTERS_DEBUG_JARPATH", "/tmp/debug-exporter.jar")
                    .withEnv("ZEEBE_BROKER_EXPORTERS_DEBUG_CLASSNAME", "io.zeebe.containers.exporter.DebugExporter")
                    .withEnv("ZEEBE_BROKER_EXPORTERS_DEBUG_ARGS_URL", "http://zeebe-dev-monitor:8080/records");
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, "zeebe");
                withEnv("ZEEBE_BROKER_NETWORK_ADVERTISEDHOST", hostName);
                return;
            } else {
                withNetwork(Network.SHARED);
            }

            if (fixedExposedPort > 0) {
                addFixedExposedPort(fixedExposedPort, DEFAULT_ZEEBE_PORT);
            } else {
                addExposedPort(DEFAULT_ZEEBE_PORT);
            }
        }

        public int getPort() {
            if (useSharedNetwork) {
                return DEFAULT_ZEEBE_PORT;
            }
            if (fixedExposedPort > 0) {
                return fixedExposedPort;
            }
            return super.getFirstMappedPort();
        }

        public String getGatewayHost() {
            return useSharedNetwork ? hostName : super.getHost();
        }
    }

}
