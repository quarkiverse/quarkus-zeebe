package io.quarkiverse.zeebe.devservices;

import java.util.Optional;
import java.util.OptionalInt;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class ZeebeDevServicesConfig {

    /**
     * If DevServices has been explicitly enabled or disabled. DevServices is generally enabled
     * by default, unless there is an existing configuration present.
     * <p>
     * When DevServices is enabled Quarkus will attempt to automatically configure and start
     * a database when running in Dev or Test mode and when Docker is running.
     */
    @ConfigItem(name = "enabled", defaultValue = "true")
    public boolean enabled;

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    @ConfigItem(name = "port")
    public OptionalInt port;

    /**
     * Indicates if the Zeebe server managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for Zeebe starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-zeebe} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @ConfigItem(name = "shared", defaultValue = "true")
    public boolean shared;

    /**
     * The value of the {@code quarkus-dev-service-zeebe} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for Zeebe looks for a container with the
     * {@code quarkus-dev-service-zeebe} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise, it
     * starts a new container with the {@code quarkus-dev-service-zeebe} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared Zeebe servers.
     */
    @ConfigItem(name = "service-name", defaultValue = "zeebe")
    public String serviceName;

    /**
     * The container image name to use, for container based DevServices providers.
     */
    @ConfigItem(name = "image-name")
    public Optional<String> imageName;

    /**
     * Helper to define the stop strategy for containers created by DevServices.
     * In particular, we don't want to actually stop the containers when they
     * have been flagged for reuse, and when the Test-containers configuration
     * has been explicitly set to allow container reuse.
     * To enable reuse, ass {@literal testcontainers.reuse.enable=true} in your
     * {@literal .testcontainers.properties} file, to be stored in your home.
     *
     * @see <a href="https://www.testcontainers.org/features/configuration/">Testcontainers Configuration</a>.
     */
    @ConfigItem(name = "reuse", defaultValue = "false")
    public boolean reuse;

    /**
     * Optional fixed debug export receiver port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    @ConfigItem(name = "test")
    public TestConfig test;

    /**
     * Debug dev mode exporter optional configuration.
     */
    @ConfigItem(name = "dev-exporter")
    public DevExporterConfig devExporter;

    /**
     * Zeebe test configuration.
     */
    @ConfigGroup
    public static class TestConfig {
        /**
         * Optional fixed debug export receiver port the dev service will listen to.
         * <p>
         * If not defined, the port will be chosen randomly.
         */
        @ConfigItem(name = "receiver-port")
        public OptionalInt receiverPort;

        /**
         * Disable or enable debug exporter for the test.
         */
        @ConfigItem(name = "exporter", defaultValue = "true")
        public boolean exporter;
    }

    /**
     * Zeebe dev mode debug exporter configuration.
     */
    @ConfigGroup
    public static class DevExporterConfig {
        /**
         * Enable or disable debug exporter.
         */
        @ConfigItem(name = "enabled", defaultValue = "true")
        public boolean enabled;

    }

}
