package io.quarkiverse.zeebe;

import io.quarkiverse.zeebe.devservices.ZeebeDevServicesConfig;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "quarkus.zeebe")
public interface ZeebeDevServiceBuildTimeConfig {

    /**
     * Default Dev services configuration.
     */
    @WithName("devservices")
    ZeebeDevServicesConfig devService();

    /**
     * Dev mode configuration.
     */
    @WithName("dev-mode")
    DevMode devMode();

    interface DevMode {

        /**
         * Disable or enabled zeebe dashboard dev-ui.
         */
        @WithName("dev-ui.enabled")
        @WithDefault("true")
        boolean devUIEnabled();

        /**
         * Observe changes in the bpmn files.
         */
        @WithName("watch-bpmn-files")
        @WithDefault("true")
        boolean watchBpmnFiles();

        /**
         * Observe changes in the bpmn directory and subdirectories.
         */
        @WithName("watch-bpmn-dir")
        @WithDefault("true")
        boolean watchBpmnDir();

        /**
         * Observe changes in the job worker.
         */
        @WithName("watch-job-worker")
        @WithDefault("true")
        boolean watchJobWorker();
    }
}
