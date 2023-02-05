package io.quarkiverse.zeebe;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.impl.ZeebeObjectMapper;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import java.util.concurrent.ScheduledExecutorService;

@Singleton
public class ZeebeResourcesProducer {

    @Produces
    @Singleton
    @Unremovable
    @DefaultBean
    public JsonMapper defaultJsonMapper() {
        return new ZeebeObjectMapper();
    }

    @Produces
    @Singleton
    @Unremovable
    @DefaultBean
    public ZeebeScheduledExecutorService defaultZeebeScheduledExecutorService() {
        return () -> Infrastructure.getDefaultWorkerPool();
    }
}
