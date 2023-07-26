package io.quarkiverse.zeebe.runtime;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;

import org.jboss.logging.Logger;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1;
import io.quarkiverse.zeebe.ZeebeClientInterceptor;

@ApplicationScoped
public class ZeebeClientService {

    private static final Logger log = Logger.getLogger(ZeebeClientService.class);

    ZeebeClient client;

    List<JobWorker> workers = new ArrayList<>();

    public ZeebeClientService(ZeebeRuntimeConfig config, JsonMapper jsonMapper,
            @Any Instance<ZeebeClientInterceptor> interceptors) {
        log.infof("Creating new zeebe client for %s", config.client.broker.gatewayAddress);
        ZeebeClientBuilder builder = ZeebeClientBuilderFactory.createBuilder(config.client);
        if (jsonMapper != null) {
            builder.withJsonMapper(jsonMapper);
        }
        interceptors.forEach(x -> builder.withInterceptors(x::interceptCall));
        client = builder.build();
    }

    @PreDestroy
    public void onStop() {
        workers.forEach(JobWorker::close);
        client.close();
    }

    @Produces
    public ZeebeClient client() {
        return client;
    }

    void openWorker(JobWorkerBuilderStep1.JobWorkerBuilderStep3 builder) {
        JobWorker tmp = builder.open();
        workers.add(tmp);
    }
}
