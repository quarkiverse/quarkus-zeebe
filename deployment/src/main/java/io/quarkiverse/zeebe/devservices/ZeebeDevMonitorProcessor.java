package io.quarkiverse.zeebe.devservices;

import static io.quarkiverse.zeebe.ZeebeProcessor.FEATURE_NAME;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.zeebe.ZeebeDevServiceBuildTimeConfig;
import io.quarkus.deployment.IsNormal;
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
import io.quarkus.runtime.LaunchMode;

public class ZeebeDevMonitorProcessor {

    private static final Logger log = Logger.getLogger(ZeebeDevMonitorProcessor.class);

    private static final String FEATURE_DEV_MONITOR = FEATURE_NAME + "-dev-monitor";
    private static final String DEV_SERVICE_MONITOR_LABEL = "quarkus-dev-service-zeebe-dev-monitor";
    public static final int DEFAULT_DEV_MONITOR_PORT = 8080;
    private static final ContainerLocator zeebeDevMonitorContainerLocator = new ContainerLocator(DEV_SERVICE_MONITOR_LABEL,
            DEFAULT_DEV_MONITOR_PORT);
    static volatile DevMonitorRunningDevService devService;
    static volatile ZeebeDevMonitorServiceCfg cfg;
    static volatile boolean first = true;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { GlobalDevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startZeebeMonitorContainers(LaunchModeBuildItem launchMode,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            ZeebeDevServiceBuildTimeConfig zeebeDevServiceBuildTimeConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            DockerStatusBuildItem dockerStatusBuildItem,
            Optional<ZeebeDevServicesProviderBuildItem> zeebeDevServiceBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem, GlobalDevServicesConfig devServicesConfig) {

        if (zeebeDevServiceBuildItem.isEmpty()) {
            log.error("The zeebe cluster activated for the dev monitor");
            return null;
        }
        String brokerUrl = zeebeDevServiceBuildItem.get().internalUrl;

        ZeebeDevMonitorServiceCfg configuration = getConfiguration(zeebeDevServiceBuildTimeConfig);

        if (devService != null) {
            boolean shouldShutdownTheBroker = !configuration.equals(cfg);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            stopDevMonitor();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Zeebe dev monitor starting:", consoleInstalledBuildItem,
                loggingSetupBuildItem);
        try {
            devService = startDevMonitor(dockerStatusBuildItem, configuration, launchMode,
                    !devServicesSharedNetworkBuildItem.isEmpty(),
                    devServicesConfig.timeout, brokerUrl);
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
                    stopDevMonitor();
                }
                first = true;
                devService = null;
                cfg = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        cfg = configuration;

        if (devService.isOwner()) {
            log.infof("The zeebe dev monitor is ready to accept connections on %s", devService.url);
        }
        return devService.toBuildItem();
    }

    private void stopDevMonitor() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop the Zeebe dev monitor", e);
            } finally {
                devService = null;
            }
        }
    }

    private DevMonitorRunningDevService startDevMonitor(DockerStatusBuildItem dockerStatusBuildItem,
            ZeebeDevMonitorServiceCfg config,
            LaunchModeBuildItem launchMode, boolean useSharedNetwork, Optional<Duration> timeout, String brokerUrl) {

        if (!config.devServicesEnabled) {
            // explicitly disabled
            log.debug("Not starting dev services for Zeebe as it has been disabled in the config");
            return null;
        }

        if (!config.monitorDevServicesEnabled) {
            // explicitly disabled
            log.debug("Not starting dev service for zeebe dev monitor as it has been disabled in the config");
            return null;
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            log.warn(
                    "Docker isn't working, please configure the Zeebe broker gateway bootstrap property (quarkus.zeebe.broker.gateway-address).");
            return null;
        }

        final Optional<ContainerAddress> maybeContainerAddress = zeebeDevMonitorContainerLocator.locateContainer(
                config.serviceName,
                config.shared,
                launchMode.getLaunchMode());

        final Supplier<DevMonitorRunningDevService> defaultZeebeDevMonitorSupplier = () -> {
            QuarkusDevMonitorContainer container = new QuarkusDevMonitorContainer(
                    DockerImageName.parse(config.imageName),
                    config.fixedExposedPort,
                    launchMode.getLaunchMode() == LaunchMode.DEVELOPMENT ? config.serviceName : null,
                    useSharedNetwork, brokerUrl);
            timeout.ifPresent(container::withStartupTimeout);
            container.start();

            return new DevMonitorRunningDevService(FEATURE_DEV_MONITOR, container.getContainerId(),
                    container::close, container.getUrl());
        };

        return maybeContainerAddress
                .map(containerAddress -> new DevMonitorRunningDevService(FEATURE_DEV_MONITOR, containerAddress.getId(),
                        null, containerAddress.getUrl()))
                .orElseGet(defaultZeebeDevMonitorSupplier);
    }

    public static class DevMonitorRunningDevService extends RunningDevService {

        private String url;

        public DevMonitorRunningDevService(String name, String containerId, Closeable closeable, String url) {
            super(name, containerId, closeable, Map.of());
            this.url = url;
        }
    }

    public static class QuarkusDevMonitorContainer extends GenericContainer<QuarkusDevMonitorContainer> {

        private final int fixedExposedPort;
        private final boolean useSharedNetwork;
        private String hostName = null;

        public QuarkusDevMonitorContainer(DockerImageName image, int fixedExposedPort,
                String serviceName, boolean useSharedNetwork, String broker) {
            super(image);

            log.debugf("Zeebe dev monitor docker image %s", image);
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
            if (serviceName != null) {
                withLabel(DEV_SERVICE_MONITOR_LABEL, serviceName);
            }
            if (this.fixedExposedPort > 0) {
                addFixedExposedPort(this.fixedExposedPort, DEFAULT_DEV_MONITOR_PORT);
            } else {
                addExposedPort(DEFAULT_DEV_MONITOR_PORT);
            }
            withNetworkAliases("zeebe-dev-monitor");
            addEnv("QUARKUS_ZEEBE_CLIENT_BROKER_GATEWAY_ADDRESS", broker);
        }

        @Override
        protected void configure() {
            super.configure();
            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, "zeebe-dev-monitor");
            } else {
                withNetwork(Network.SHARED);
            }
        }

        public String getHostName() {
            return useSharedNetwork ? hostName : super.getHost();
        }

        public String getUrl() {
            String url = "http://" + getHost() + ":";
            if (fixedExposedPort > 0) {
                url = url + fixedExposedPort;
            } else {
                url = url + getMappedPort(DEFAULT_DEV_MONITOR_PORT);
            }
            return url;
        }
    }

    private ZeebeDevMonitorServiceCfg getConfiguration(ZeebeDevServiceBuildTimeConfig cfg) {
        ZeebeDevServicesConfig devServicesConfig = cfg.devService;
        return new ZeebeDevMonitorServiceCfg(devServicesConfig);
    }

    private static final class ZeebeDevMonitorServiceCfg {
        private final boolean devServicesEnabled;

        private final boolean monitorDevServicesEnabled;
        private final String imageName;
        private final int fixedExposedPort;
        private final String serviceName;
        private final boolean shared;

        private final String zeebeServiceName;

        public ZeebeDevMonitorServiceCfg(ZeebeDevServicesConfig config) {
            this.devServicesEnabled = config.enabled;
            this.monitorDevServicesEnabled = config.monitor.enabled;
            this.imageName = config.monitor.imageName;
            this.fixedExposedPort = config.monitor.port.orElse(0);
            this.serviceName = config.monitor.serviceName;
            this.shared = true;
            this.zeebeServiceName = config.serviceName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ZeebeDevMonitorServiceCfg that = (ZeebeDevMonitorServiceCfg) o;
            return devServicesEnabled == that.devServicesEnabled && Objects.equals(imageName, that.imageName)
                    && Objects.equals(fixedExposedPort, that.fixedExposedPort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devServicesEnabled, imageName, fixedExposedPort);
        }
    }
}
