package io.quarkiverse.zeebe;

import io.quarkiverse.zeebe.devservices.ZeebeDevServicesConfig;
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

}
