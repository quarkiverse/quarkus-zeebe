package io.quarkiverse.zeebe.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "zeebe", phase = ConfigPhase.RUN_TIME)
public class ZeebeRuntimeConfig {

    /**
     * Zeebe client broker configuration.
     */
    @ConfigItem(name = "client")
    public ZeebeClientRuntimeConfig client = new ZeebeClientRuntimeConfig();

    /**
     * Zeebe client is active
     */
    @ConfigItem(name = "active", defaultValue = "true")
    public boolean active = true;

}
