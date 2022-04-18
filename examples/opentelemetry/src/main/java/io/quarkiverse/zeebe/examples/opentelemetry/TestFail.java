package io.quarkiverse.zeebe.examples.opentelemetry;

import javax.inject.Inject;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeWorker(name = "test.fail.action", type = "test.fail")
public class TestFail implements JobHandler {

    @Inject
    TestService service;

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        Parameter p = job.getVariablesAsType(Parameter.class);
        p.info = "test.fail";
        p.data = service.getParam();
        client.newFailCommand(job).retries(job.getRetries() - 1)
                .errorMessage("Fail custom error message").send().join();
    }
}
