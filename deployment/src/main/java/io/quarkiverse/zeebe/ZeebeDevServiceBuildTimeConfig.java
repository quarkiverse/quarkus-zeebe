package io.quarkiverse.zeebe;

import io.quarkiverse.zeebe.devservices.ZeebeDevServicesConfig;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "zeebe", phase = ConfigPhase.BUILD_TIME)
public class ZeebeDevServiceBuildTimeConfig {

    /**
     * Default Dev services configuration.
     */
    @ConfigItem(name = "devservices")
    public ZeebeDevServicesConfig devService;

    /**
     * Dev mode configuration.
     */
    @ConfigItem(name = "dev-mode")
    public DevMode devMode;

    @ConfigGroup
    public static class DevMode {

        /**
         * Disable or enabled zeebe dashboard dev-ui.
         */
        @ConfigItem(name = "dev-ui.enabled", defaultValue = "true")
        public boolean devUIEnabled;
        /**
         * Observe changes in the bpmn files.
         */
        @ConfigItem(name = "watch-bpmn-files", defaultValue = "true")
        public boolean watchBpmnFiles = true;
        /**
         * Observe changes in the bpmn directory and subdirectories.
         */
        @ConfigItem(name = "watch-bpmn-dir", defaultValue = "true")
        public boolean watchBpmnDir = true;
        /**
         * Observe changes in the job worker.
         */
        @ConfigItem(name = "watch-job-worker", defaultValue = "true")
        public boolean watchJobWorker = true;
    }
}
