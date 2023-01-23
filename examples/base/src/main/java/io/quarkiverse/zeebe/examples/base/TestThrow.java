package io.quarkiverse.zeebe.examples.base;

import javax.inject.Inject;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeWorker(name = "test.throw.action", type = "test.throw")
public class TestThrow implements JobHandler {

    @Inject
    TestService service;

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        Parameter p = job.getVariablesAsType(Parameter.class);
        p.info = "test.throw";
        p.data = service.getParam();
        client.newThrowErrorCommand(job).errorCode("throw custom error code")
                .errorMessage("throw custom error message")
                .send().join();
    }
}
