package io.quarkiverse.zeebe.runtime;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "zeebe", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class ZeebeBuildTimeConfig {

    /**
     * Zeebe client resources configuration.
     */
    @ConfigItem(name = "resources")
    public ResourcesConfig resources = new ResourcesConfig();

    /**
     * Health check configuration.
     */
    @ConfigItem(name = "health")
    public HealthCheckConfig health = new HealthCheckConfig();

    /**
     * Opentracing configuration.
     */
    @ConfigItem(name = "opentracing")
    public OpentracingConfig opentracing = new OpentracingConfig();

    /**
     * Health check configuration.
     */
    @ConfigGroup
    public static class HealthCheckConfig {
        /**
         * Whether or not an health check is published in case the smallrye-health extension is present.
         */
        @ConfigItem(name = "enabled")
        public boolean enabled;
    }

    /**
     * Zeebe client resources configuration.
     */
    @ConfigGroup
    public static class ResourcesConfig {
        /**
         * BPMN process root folder. Default bpmn
         */
        @ConfigItem(name = "location", defaultValue = "bpmn")
        public String location;

    }

    /**
     * Opentracing configuration.
     */
    @ConfigGroup
    public static class OpentracingConfig {
        /**
         * Whether or not an opentracing is published in case the smallrye-opentracing extension is present.
         */
        @ConfigItem(name = "enabled")
        public boolean enabled;
    }
}
