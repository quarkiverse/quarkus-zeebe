package io.quarkiverse.zeebe.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
@ConfigMapping(prefix = "quarkus.zeebe")
public interface ZeebeBuildTimeConfig {

    /**
     * Zeebe client resources configuration.
     */
    @WithName("resources")
    ResourcesConfig resources();

    /**
     * Metrics configuration.
     */
    @WithName("metrics")
    MetricsConfig metrics();

    /**
     * Health check configuration.
     */
    @WithName("health")
    HealthCheckConfig health();

    /**
     * Tracing configuration.
     */
    @WithName("tracing")
    TracingConfig tracing();

    /**
     * Metrics configuration.
     */
    interface MetricsConfig {
        /**
         * Whether a metrics is enabled in case the micrometer or micro-profile metrics extension is present.
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();
    }

    /**
     * Health check configuration.
     */
    interface HealthCheckConfig {
        /**
         * Whether a health check is published in case the smallrye-health extension is present.
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();
    }

    /**
     * Zeebe client resources configuration.
     */
    interface ResourcesConfig {
        /**
         * Whether an auto scan BPMN process folder. Default true
         */
        @WithName("enabled")
        @WithDefault("true")
        Boolean enabled();

        /**
         * BPMN process root folder. Default bpmn
         */
        @WithName("location")
        @WithDefault("bpmn")
        String location();

    }

    /**
     * Tracing configuration.
     */
    interface TracingConfig {
        /**
         * Whether an opentracing is published in case the smallrye-opentracing extension is present.
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();
    }

}
