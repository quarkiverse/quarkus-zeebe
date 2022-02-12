package io.quarkiverse.zeebe.devservices;

import java.util.Objects;
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
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    @ConfigItem
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
    @ConfigItem(defaultValue = "true")
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
    @ConfigItem(defaultValue = "zeebe")
    public String serviceName;

    /**
     * The container image name to use, for container based DevServices providers.
     */
    @ConfigItem(name = "image-name")
    public Optional<String> imageName;

    /**
     * Zeebe broker with hazelcast exporter
     */
    @ConfigItem(name = "hazelcast")
    public HazelcastConfig hazelcast = new HazelcastConfig();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ZeebeDevServicesConfig that = (ZeebeDevServicesConfig) o;
        return enabled == that.enabled &&
                Objects.equals(port, that.port) &&
                Objects.equals(shared, that.shared) &&
                Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, port, shared, serviceName);
    }

    /**
     * Zeebe broker hazelcast configuration.
     */
    @ConfigGroup
    public static class HazelcastConfig {

        /**
         * Enable or disable hazelcast exporter for devservices.
         */
        @ConfigItem(defaultValue = "true")
        public boolean enabled;

        /**
         * Optional fixed port the hazelcast will listen to.
         * <p>
         * If not defined, the port will be chosen randomly.
         */
        @ConfigItem
        public OptionalInt port;

        /**
         * The container image name to use, for container based zeebe broker hazelcast providers.
         */
        @ConfigItem(name = "image-name", defaultValue = "ghcr.io/camunda-community-hub/zeebe-with-hazelcast-exporter:1.3.3-1.1.1-SNAPSHOT")
        public Optional<String> imageName;
    }
}
