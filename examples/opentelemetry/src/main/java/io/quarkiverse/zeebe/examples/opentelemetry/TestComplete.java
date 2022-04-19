package io.quarkiverse.zeebe.examples.opentelemetry;

import javax.inject.Inject;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeWorker(name = "test.complete.action", type = "test.complete")
public class TestComplete implements JobHandler {

    @Inject
    TestService service;

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        Parameter p = job.getVariablesAsType(Parameter.class);
        p.info = "test.complete";
        p.data = service.getParam();
        client.newCompleteCommand(job.getKey()).variables(p).send().join();
    }
}
