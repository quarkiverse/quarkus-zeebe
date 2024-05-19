package io.quarkiverse.zeebe.devservices;

import static io.quarkiverse.zeebe.ZeebeProcessor.FEATURE_NAME;
import static io.quarkus.runtime.LaunchMode.DEVELOPMENT;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import io.camunda.zeebe.client.ZeebeClient;
import io.quarkiverse.zeebe.ZeebeDevServiceBuildTimeConfig;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.zeebe.containers.*;
import io.zeebe.containers.util.HostPortForwarder;

public class ZeebeDevServiceProcessor {

    private static final String DEFAULT_ZEEBE_CONTAINER_IMAGE = "camunda/zeebe";

    private static final String DEFAULT_ZEEBE_VERSION = ZeebeClient.class.getPackage().getImplementationVersion();

    private static DockerImageName ZEEBE_IMAGE_NAME = DockerImageName.parse(DEFAULT_ZEEBE_CONTAINER_IMAGE)
            .withTag(DEFAULT_ZEEBE_VERSION);

    private static final Logger log = Logger.getLogger(ZeebeDevServiceProcessor.class);
    static final String PROP_ZEEBE_GATEWAY_ADDRESS = "quarkus.zeebe.client.broker.gateway-address";
    static final String PROP_ZEEBE_REST_ADDRESS = "quarkus.zeebe.client.broker.rest-address";
    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-zeebe";
    public static final int DEFAULT_ZEEBE_PORT = ZeebePort.GATEWAY.getPort();
    public static final int DEFAULT_ZEEBE_REST_PORT = 8080;
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
        }

        return devService.toBuildItem();
    }

    public static class ZeebeRunningDevService extends RunningDevService {

        public ZeebeRunningDevService(String name, String containerId, Closeable closeable, Map<String, String> config,
                String zeebeInternalUrl) {
            super(name, containerId, closeable, config);
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

            DockerImageName image = ZEEBE_IMAGE_NAME;
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
                    config.devDebugExporter,
                    config.debugReceiverPort,
                    config.fixedExposedRestPort);
            timeout.ifPresent(container::withStartupTimeout);

            // enable test-container reuse
            if (config.reuse) {
                container.withReuse(true);
            }

            container.start();

            String gateway = String.format("%s:%d", container.getZeebeHost(), container.getGrpcPort());
            String baseUrl = String.format("http://%s:%d", container.getZeebeHost(), container.getRestPort());
            String zeebeInternalUrl = container.getInternalAddress(DEFAULT_ZEEBE_PORT);
            String testClient = container.getExternalAddress(DEFAULT_ZEEBE_PORT);
            String testClientRest = container.getExternalAddress(DEFAULT_ZEEBE_REST_PORT);

            return new ZeebeRunningDevService(FEATURE_NAME,
                    container.getContainerId(),
                    new ContainerShutdownCloseable(container, FEATURE_NAME),
                    configMap(gateway, baseUrl, launchMode.isTest(), testClient, testClientRest, testDebugExportPort,
                            config.testExporter),
                    zeebeInternalUrl);
        };

        return maybeContainerAddress
                .map(containerAddress -> new ZeebeRunningDevService(FEATURE_NAME,
                        containerAddress.getId(),
                        null, configMap(containerAddress.getUrl(), containerAddress.getUrl(), false, null, null, null, false),
                        null))
                .orElseGet(defaultZeebeBrokerSupplier);
    }

    private static Map<String, String> configMap(String gateway, String baseUrl, boolean test, String testClient,
            String testClientRest,
            Integer testDebugExportPort,
            boolean testExporter) {
        Map<String, String> config = new HashMap<>();
        config.put(PROP_ZEEBE_GATEWAY_ADDRESS, gateway);
        config.put(PROP_ZEEBE_REST_ADDRESS, baseUrl);
        if (test && testExporter) {
            if (testDebugExportPort != null) {
                config.put("quarkiverse.zeebe.devservices.test.receiver-port", "" + testDebugExportPort);
            }
            if (testClient != null) {
                config.put("quarkiverse.zeebe.devservices.test.gateway-address", testClient);
                config.put("quarkiverse.zeebe.devservices.test.rest-address", testClientRest);
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
        private final Integer fixedExposedRestPort;
        private final boolean shared;
        private final String serviceName;

        private final boolean testExporter;
        private final int testDebugExportPort;

        private final boolean devDebugExporter;

        private final int debugReceiverPort;

        private final boolean reuse;

        public ZeebeDevServiceCfg(ZeebeDevServicesConfig config) {
            this.devServicesEnabled = config.enabled;
            this.imageName = config.imageName.orElse(null);
            this.fixedExposedPort = config.port.orElse(0);
            this.fixedExposedRestPort = config.restPort.orElse(0);
            this.shared = config.shared;
            this.serviceName = config.serviceName;
            this.testExporter = config.test.exporter;
            this.testDebugExportPort = config.test.receiverPort.orElse(0);
            this.devDebugExporter = config.devExporter.enabled;
            this.debugReceiverPort = getPort();
            this.reuse = config.reuse;
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
        private final int fixedExposedRestPort;
        private final boolean useSharedNetwork;

        private String hostName = null;

        public QuarkusZeebeContainer(DockerImageName image, int fixedExposedPort, String serviceName,
                boolean useSharedNetwork, boolean test, int testDebugExportPort, boolean devDebugExporter,
                int debugExporterPort, int fixedExposedRestPort) {
            super(image);
            log.debugf("Zeebe broker docker image %s", image);
            this.fixedExposedPort = fixedExposedPort;
            this.fixedExposedRestPort = fixedExposedRestPort;
            this.useSharedNetwork = useSharedNetwork;

            if (serviceName != null) {
                withLabel(DEV_SERVICE_LABEL, serviceName);
            }
            if (test) {
                // create random port
                withDebugExporter(testDebugExportPort);
            } else {
                if (devDebugExporter) {
                    debugExporter(debugExporterPort);
                }
            }
        }

        public void debugExporter(final int port) {
            final int containerPort = HostPortForwarder.forwardHostPort(port, 5);
            var receiver = "http://host.testcontainers.internal:" + containerPort + "/q/zeebe/records";
            //noinspection resource
            withCopyToContainer(
                    MountableFile.forClasspathResource("debug-exporter.jar"), "/tmp/debug-exporter.jar")
                    .withEnv("ZEEBE_BROKER_EXPORTERS_DEBUG_JARPATH", "/tmp/debug-exporter.jar")
                    .withEnv(
                            "ZEEBE_BROKER_EXPORTERS_DEBUG_CLASSNAME", "io.zeebe.containers.exporter.DebugExporter")
                    .withEnv(
                            "ZEEBE_BROKER_EXPORTERS_DEBUG_ARGS_URL", receiver);
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, "zeebe");
                addExposedPort(DEFAULT_ZEEBE_REST_PORT);
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
            if (fixedExposedRestPort > 0) {
                addFixedExposedPort(fixedExposedRestPort, DEFAULT_ZEEBE_REST_PORT);
            } else {
                addExposedPort(DEFAULT_ZEEBE_REST_PORT);
            }
        }

        public int getGrpcPort() {
            if (useSharedNetwork) {
                return DEFAULT_ZEEBE_PORT;
            }
            if (fixedExposedPort > 0) {
                return fixedExposedPort;
            }
            return super.getFirstMappedPort();
        }

        public int getRestPort() {
            if (useSharedNetwork) {
                return DEFAULT_ZEEBE_REST_PORT;
            }
            if (fixedExposedPort > 0) {
                return fixedExposedRestPort;
            }
            return super.getFirstMappedPort();
        }

        public String getZeebeHost() {
            return useSharedNetwork ? hostName : super.getHost();
        }
    }

    private static int getPort() {
        Config config = ConfigProvider.getConfig();
        return config
                .getOptionalValue("quarkus.http.port", Integer.class)
                .orElse(8080);
    }
}
