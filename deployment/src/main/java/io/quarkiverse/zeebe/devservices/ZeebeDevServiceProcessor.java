package io.quarkiverse.zeebe.devservices;

import static io.quarkus.runtime.LaunchMode.DEVELOPMENT;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.zeebe.ZeebeDevServiceBuildTimeConfig;
import io.quarkus.deployment.IsDockerWorking;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesConfigResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.zeebe.containers.*;

public class ZeebeDevServiceProcessor {

    private static final Logger log = Logger.getLogger(ZeebeDevServiceProcessor.class);
    /**
     * Label to add to shared Dev Service for Zeebe running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-zeebe";
    public static final int DEFAULT_ZEEBE_PORT = ZeebePort.GATEWAY.getPort();
    public static final int DEFAULT_HAZELCAST_PORT = 5701;
    private static final String QUARKUS = "quarkus.";
    private static final String DOT = ".";

    private static final ContainerLocator zeebeContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL, DEFAULT_ZEEBE_PORT);
    private static volatile List<Closeable> closeables;
    private static volatile ZeebeDevServiceBuildTimeConfig.DevServiceConfiguration capturedDevServicesConfiguration;
    private static volatile boolean first = true;
    private static volatile Boolean dockerRunning = null;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { GlobalDevServicesConfig.Enabled.class })
    public void startZeebeContainers(LaunchModeBuildItem launchMode,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            BuildProducer<DevServicesConfigResultBuildItem> devConfigProducer,
            ZeebeDevServiceBuildTimeConfig config,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem, GlobalDevServicesConfig devServicesConfig,
            BuildProducer<ZeebeDevServicesProviderBuildItem> startResultProducer) {

        // figure out if we need to shut down and restart existing Zeebe containers
        // if not and the Zeebe containers have already started we just return
        if (closeables != null) {
            boolean restartRequired = !config.devService.equals(capturedDevServicesConfiguration);
            if (!restartRequired) {
                return;
            }
            for (Closeable closeable : closeables) {
                try {
                    closeable.close();
                } catch (Throwable e) {
                    log.error("Failed to stop Zeebe container", e);
                }
            }
            closeables = null;
            capturedDevServicesConfiguration = null;
        }

        capturedDevServicesConfiguration = config.devService;
        ZeebeDevServicesConfig zeebeConfig = capturedDevServicesConfiguration.devservices;
        List<Closeable> currentCloseables = new ArrayList<>();

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Zeebe Dev Services Starting:", consoleInstalledBuildItem,
                loggingSetupBuildItem);
        try {
            ZeebeDevServicesStartResult startResult = startContainer(zeebeConfig,
                    launchMode.getLaunchMode(),
                    !devServicesSharedNetworkBuildItem.isEmpty(), devServicesConfig.timeout);
            if (startResult == null) {
                compressor.close();
                return;
            }
            currentCloseables.add(startResult.closeable);

            System.setProperty("quarkus.zeebe.devservices.test.hazelcast", startResult.hazelcast);
            System.setProperty("quarkus.zeebe.devservices.test.gateway-address", startResult.client);
            devConfigProducer
                    .produce(new DevServicesConfigResultBuildItem(getConfigPrefix() + "broker.gateway-address",
                            startResult.gateway));
            log.infof("The zeebe server is ready to accept connections on %s", startResult.gateway);
            startResultProducer.produce(new ZeebeDevServicesProviderBuildItem(startResult));

            compressor.close();
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        closeables = currentCloseables;

        if (first) {
            first = false;
            Runnable closeTask = () -> {
                dockerRunning = null;
                if (closeables != null) {
                    for (Closeable closeable : closeables) {
                        try {
                            closeable.close();
                        } catch (Throwable t) {
                            log.error("Failed to stop zeebe", t);
                        }
                    }
                }
                first = true;
                closeables = null;
                capturedDevServicesConfiguration = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
    }

    private ZeebeDevServicesStartResult startContainer(
            ZeebeDevServicesConfig devServicesConfig, LaunchMode launchMode,
            boolean useSharedNetwork, Optional<Duration> timeout) {

        if (!devServicesConfig.enabled) {
            // explicitly disabled
            log.debug("Not starting devservices for Zeebe as it has been disabled in the config");
            return null;
        }

        String configPrefix = getConfigPrefix();

        boolean needToStart = !ConfigUtils.isPropertyPresent(configPrefix + "broker.gateway-address");
        if (!needToStart) {
            log.debug("Not starting devservices for Zeebe as 'broker.gateway-address' have been provided");
            return null;
        }

        if (dockerRunning == null) {
            dockerRunning = new IsDockerWorking.IsDockerRunningSilent().getAsBoolean();
        }

        if (!dockerRunning) {
            log.warn("Please configure 'quarkus.broker.gateway-address' or get a working docker instance");
            return null;
        }

        Optional<DockerImageName> image;
        if (devServicesConfig.hazelcast.enabled) {
            image = devServicesConfig.hazelcast.imageName.map(DockerImageName::parse);
        } else {
            image = devServicesConfig.imageName.map(DockerImageName::parse);
        }

        Supplier<ZeebeDevServicesStartResult> defaultZeebeServerSupplier = () -> {
            QuarkusZeebeContainer zeebeContainer = new QuarkusZeebeContainer(
                    image.orElse(ZeebeDefaults.getInstance().getDefaultDockerImage()),
                    devServicesConfig.port,
                    launchMode == DEVELOPMENT ? devServicesConfig.serviceName : null,
                    useSharedNetwork,
                    devServicesConfig.hazelcast.enabled);
            timeout.ifPresent(zeebeContainer::withStartupTimeout);
            zeebeContainer.start();
            String gateway = zeebeContainer.getGatewayHost() + ":" + zeebeContainer.getPort();
            String client = zeebeContainer.getExternalAddress(DEFAULT_ZEEBE_PORT);
            String hazelcast = "";
            if (devServicesConfig.hazelcast.enabled) {
                hazelcast = zeebeContainer.getExternalAddress(DEFAULT_HAZELCAST_PORT);
            }
            return new ZeebeDevServicesStartResult(gateway, client, hazelcast, zeebeContainer::close);
        };

        return zeebeContainerLocator.locateContainer(devServicesConfig.serviceName, devServicesConfig.shared, launchMode)
                .map(containerAddress -> new ZeebeDevServicesStartResult(containerAddress.getUrl(), null, null, null))
                .orElseGet(defaultZeebeServerSupplier);

    }

    private String getConfigPrefix() {
        return QUARKUS + "zeebe.client" + DOT;
    }

    private static class QuarkusZeebeContainer extends ZeebeContainer {

        private final OptionalInt fixedExposedPort;
        private final boolean useSharedNetwork;

        private String hostName = null;

        public QuarkusZeebeContainer(DockerImageName image, OptionalInt fixedExposedPort, String serviceName,
                boolean useSharedNetwork, boolean hazelcast) {
            super(image);
            log.debugf("Zeebe broker docker image %s", image);
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
            if (serviceName != null) {
                withLabel(DEV_SERVICE_LABEL, serviceName);
            }
            if (hazelcast) {
                addExposedPort(DEFAULT_HAZELCAST_PORT);
                addExposedPort(ZeebePort.MONITORING.getPort());
            }
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, "zeebe");
                withEnv("ZEEBE_BROKER_NETWORK_ADVERTISEDHOST", hostName);
                return;
            }

            if (fixedExposedPort.isPresent()) {
                addFixedExposedPort(fixedExposedPort.getAsInt(), DEFAULT_ZEEBE_PORT);
            } else {
                addExposedPort(DEFAULT_ZEEBE_PORT);
            }
        }

        public int getPort() {
            if (useSharedNetwork) {
                return DEFAULT_ZEEBE_PORT;
            }

            if (fixedExposedPort.isPresent()) {
                return fixedExposedPort.getAsInt();
            }
            return super.getFirstMappedPort();
        }

        public String getGatewayHost() {
            return useSharedNetwork ? hostName : super.getHost();
        }
    }

}
