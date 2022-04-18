package io.quarkiverse.zeebe.examples.opentelemetry;

import javax.inject.Inject;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeWorker(name = "test.exception.action", type = "test.exception")
public class TestException implements JobHandler {

    @Inject
    TestService service;

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        Parameter p = job.getVariablesAsType(Parameter.class);
        p.info = "test.exception";
        p.data = service.getParam();
        throw new RuntimeException("Error custom exception message");
    }
}
