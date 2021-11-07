package io.quarkiverse.zeebe.runtime;

import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.camunda.zeebe.client.api.worker.JobHandler;

@ApplicationScoped
public class ZeebeWorkerContainer {

    @Inject
    @Any
    Instance<JobHandler> jobHandlers;

    public Map<String, JobHandler> getJobHandlers() {
        return jobHandlers.stream().collect(Collectors.toMap(p -> p.getClass().getName(), p -> p));
    }
}
