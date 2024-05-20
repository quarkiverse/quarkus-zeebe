package io.quarkiverse.zeebe.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;

import org.jboss.logging.Logger;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.JsonMapper;
import io.quarkiverse.zeebe.ZeebeClientInterceptor;
import io.quarkiverse.zeebe.runtime.noop.NoOpZeebeClient;

@ApplicationScoped
public class ZeebeClientService {

    private static final Logger log = Logger.getLogger(ZeebeClientService.class);

    ZeebeClient client;

    public ZeebeClientService(ZeebeRuntimeConfig config, JsonMapper jsonMapper,
            @Any Instance<ZeebeClientInterceptor> interceptors) {
        if (config.active) {
            log.infof("Creating new zeebe client for %s", config.client.broker.gatewayAddress);
            ZeebeClientBuilder builder = ZeebeClientBuilderFactory.createBuilder(config.client, jsonMapper);
            interceptors.forEach(x -> builder.withInterceptors(x::interceptCall));
            client = builder.build();
        } else {
            log.infof("Zeebe extension is disabled");
            client = new NoOpZeebeClient();
        }
    }

    @Produces
    public ZeebeClient client() {
        return client;
    }

}
