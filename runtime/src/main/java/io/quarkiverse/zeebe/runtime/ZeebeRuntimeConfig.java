package io.quarkiverse.zeebe.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.zeebe")
public interface ZeebeRuntimeConfig {

    /**
     * Zeebe client broker configuration.
     */
    @WithName("client")
    ZeebeClientRuntimeConfig client();

    /**
     * Zeebe client is active
     */
    @WithName("active")
    @WithDefault("true")
    boolean active();

}
