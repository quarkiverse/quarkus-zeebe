package io.quarkiverse.zeebe;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.impl.ZeebeObjectMapper;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@Singleton
public class ZeebeResourcesProducer {

    protected final ObjectMapper objectMapper;

    @Inject
    public ZeebeResourcesProducer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Produces
    @Singleton
    @Unremovable
    @DefaultBean
    public JsonMapper defaultJsonMapper() {
        return new ZeebeObjectMapper(objectMapper);
    }

    @Produces
    @Singleton
    @Unremovable
    @DefaultBean
    public ZeebeScheduledExecutorService defaultZeebeScheduledExecutorService() {
        return Infrastructure::getDefaultWorkerPool;
    }
}
