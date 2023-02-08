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
     * Metrics configuration.
     */
    @ConfigItem(name = "metrics")
    public MetricsConfig metrics = new MetricsConfig();

    /**
     * Health check configuration.
     */
    @ConfigItem(name = "health")
    public HealthCheckConfig health = new HealthCheckConfig();

    /**
     * Tracing configuration.
     */
    @ConfigItem(name = "tracing")
    public TracingConfig tracing = new TracingConfig();

    /**
     * Metrics configuration.
     */
    @ConfigGroup
    public static class MetricsConfig {
        /**
         * Whether a metrics is enabled in case the micrometer or micro-profile metrics extension is present.
         */
        @ConfigItem(name = "enabled", defaultValue = "true")
        public boolean enabled;
    }

    /**
     * Health check configuration.
     */
    @ConfigGroup
    public static class HealthCheckConfig {
        /**
         * Whether a health check is published in case the smallrye-health extension is present.
         */
        @ConfigItem(name = "enabled", defaultValue = "true")
        public boolean enabled;
    }

    /**
     * Zeebe client resources configuration.
     */
    @ConfigGroup
    public static class ResourcesConfig {
        /**
         * Whether an auto scan BPMN process folder. Default true
         */
        @ConfigItem(name = "enabled", defaultValue = "true")
        public Boolean enabled;
        /**
         * BPMN process root folder. Default bpmn
         */
        @ConfigItem(name = "location", defaultValue = "bpmn")
        public String location;

    }

    /**
     * Tracing configuration.
     */
    @ConfigGroup
    public static class TracingConfig {
        /**
         * Whether an opentracing is published in case the smallrye-opentracing extension is present.
         */
        @ConfigItem(name = "enabled", defaultValue = "true")
        public boolean enabled = true;
    }

}
