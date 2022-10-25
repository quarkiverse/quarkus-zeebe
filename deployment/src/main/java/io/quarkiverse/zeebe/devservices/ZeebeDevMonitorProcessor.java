package io.quarkiverse.zeebe.devservices;

import static io.quarkiverse.zeebe.ZeebeProcessor.FEATURE_NAME;
import static io.quarkus.runtime.LaunchMode.DEVELOPMENT;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.zeebe.ZeebeDevServiceBuildTimeConfig;
import io.quarkus.deployment.IsDockerWorking;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.LaunchMode;

public class ZeebeDevMonitorDevServiceProcessor {

    private static final Logger log = Logger.getLogger(ZeebeDevMonitorDevServiceProcessor.class);
    
    private static final String DEV_SERVICE_MONITOR_LABEL = "quarkus-dev-service-zeebe-dev-monitor";
    public static final int DEFAULT_SIMPLE_MONITOR_PORT = 8082;

    private static volatile List<Closeable> closeables;
    private static volatile ZeebeDevServiceBuildTimeConfig.DevServiceConfiguration capturedDevServicesConfiguration;
    private static volatile boolean first = true;
    private static volatile Boolean dockerRunning = null;

    private static final ContainerLocator zeebeMonitorContainerLocator = new ContainerLocator(DEV_SERVICE_MONITOR_LABEL,
            DEFAULT_SIMPLE_MONITOR_PORT);

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { GlobalDevServicesConfig.Enabled.class })
    public void startZeebeMonitorContainers(LaunchModeBuildItem launchMode,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            ZeebeDevServiceBuildTimeConfig config,
            BuildProducer<DevServicesResultBuildItem> devConfigProducer,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            Optional<ZeebeDevServicesProviderBuildItem> zeebeDevServiceBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem, GlobalDevServicesConfig devServicesConfig) {

        if (zeebeDevServiceBuildItem.isEmpty()) {
            return;
        }

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
                (launchMode.isTest() ? "(test) " : "") + "Zeebe monitor Dev Services Starting:", consoleInstalledBuildItem,
                loggingSetupBuildItem);
        try {

            ZeebeDevServiceProcessor.ZeebeDevServicesStartResult zeebResult = zeebeDevServiceBuildItem.get().result;
            ZeebeSimpleMonitorDevServicesStartResult startResult = startSimpleMonitorContainer(zeebeConfig,
                    launchMode.getLaunchMode(),
                    !devServicesSharedNetworkBuildItem.isEmpty(), devServicesConfig.timeout,
                    zeebResult.internalBroker, zeebResult.internalHazelcast);
            if (startResult == null) {
                compressor.close();
                return;
            }
            currentCloseables.add(startResult.getCloseable());

            compressor.close();
            log.infof("The zeebe monitor is ready to accept connections on %s", startResult.url);
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

    private ZeebeSimpleMonitorDevServicesStartResult startSimpleMonitorContainer(
            ZeebeDevServicesConfig devServicesConfig, LaunchMode launchMode,
            boolean useSharedNetwork, Optional<Duration> timeout, String broker, String hazelcast) {

        if (!devServicesConfig.enabled) {
            // explicitly disabled
            log.debug("Not starting devservices for Zeebe as it has been disabled in the config");
            return null;
        }

        ZeebeDevServicesConfig.MonitorConfig monitorConfig = devServicesConfig.monitor;

        if (!monitorConfig.enabled) {
            // explicitly disabled
            log.debug("Not starting devservices for Zeebe monitor as it has been disabled in the config");
            return null;
        }

        if (dockerRunning == null) {
            dockerRunning = new IsDockerWorking.IsDockerRunningSilent().getAsBoolean();
        }

        if (!dockerRunning) {
            log.warn("Please configure 'quarkus.broker.gateway-address' or get a working docker instance");
            return null;
        }

        DockerImageName image = DockerImageName.parse(monitorConfig.imageName);
        Supplier<ZeebeSimpleMonitorDevServicesStartResult> defaultZeebeServerSupplier = () -> {
            QuarkusSimpleMonitorContainer zeebeContainer = new QuarkusSimpleMonitorContainer(
                    image,
                    monitorConfig.port,
                    launchMode == DEVELOPMENT ? monitorConfig.serviceName : null,
                    useSharedNetwork,
                    broker, hazelcast);
            timeout.ifPresent(zeebeContainer::withStartupTimeout);
            zeebeContainer.start();

            String url = "http://" + zeebeContainer.getHost() + ":";
            if (devServicesConfig.monitor.port.isPresent()) {
                url = url + devServicesConfig.monitor.port;
            } else {
                url = url + zeebeContainer.getMappedPort(DEFAULT_SIMPLE_MONITOR_PORT);
            }
            return new ZeebeSimpleMonitorDevServicesStartResult(zeebeContainer.getContainerId(), url, zeebeContainer::close);
        };

        return zeebeMonitorContainerLocator
                .locateContainer(devServicesConfig.monitor.serviceName, devServicesConfig.shared, launchMode)
                .map(containerAddress -> new ZeebeSimpleMonitorDevServicesStartResult(null, containerAddress.getUrl(), null))
                .orElseGet(defaultZeebeServerSupplier);

    }

    public static class QuarkusSimpleMonitorContainer extends GenericContainer<QuarkusSimpleMonitorContainer> {

        private final OptionalInt fixedExposedPort;
        private final boolean useSharedNetwork;
        private String hostName = null;

        public QuarkusSimpleMonitorContainer(DockerImageName image, OptionalInt fixedExposedPort,
                String serviceName, boolean useSharedNetwork, String broker, String hazelcast) {
            super(image);

            log.debugf("Zeebe simple monitor docker image %s", image);
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
            if (serviceName != null) {
                withLabel(DEV_SERVICE_MONITOR_LABEL, serviceName);
            }
            if (this.fixedExposedPort.isPresent()) {
                addFixedExposedPort(this.fixedExposedPort.getAsInt(), 8082);
            } else {
                addExposedPort(DEFAULT_SIMPLE_MONITOR_PORT);
            }
            addEnv("zeebe.client.broker.gateway-address", broker);
        }

        @Override
        protected void configure() {
            super.configure();
            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, "zeebe-simple-monitor");
            } else {
                withNetwork(Network.SHARED);
            }
        }

        public String getHostName() {
            return hostName;
        }
    }

    public static class ZeebeSimpleMonitorDevServicesStartResult extends DevServicesResultBuildItem.RunningDevService {
        public final String url;

        public ZeebeSimpleMonitorDevServicesStartResult(String containerId, String url, Closeable closeable) {
            super(FEATURE_NAME + "-dev-monitor", containerId, closeable, Map.of());
            this.url = url;
        }
    }
}
