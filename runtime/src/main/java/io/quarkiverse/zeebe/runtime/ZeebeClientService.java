package io.quarkiverse.zeebe.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

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

    ZeebeRuntimeConfig config;

    ZeebeClient client;

    @Inject
    JsonMapper jsonMapper;

    @Inject
    @Any
    Instance<ZeebeClientInterceptor> interceptors;

    List<JobWorker> workers = new ArrayList<>();

    void initialize(ZeebeRuntimeConfig config) {
        log.infof("Creating new zeebe client for %s", config.broker.gatewayAddress);
        this.config = config;
        ZeebeClientBuilder builder = ZeebeClientBuilderFactory.createBuilder(config);
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
