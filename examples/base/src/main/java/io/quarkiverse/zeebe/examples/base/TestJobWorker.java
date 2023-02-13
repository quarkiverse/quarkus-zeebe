package io.quarkiverse.zeebe.examples.base;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkiverse.zeebe.JobWorker;
import io.quarkiverse.zeebe.Variable;
import io.quarkiverse.zeebe.VariablesAsType;

@ApplicationScoped
public class TestJobWorker {

    @Inject
    TestService service;

    @JobWorker(name = "test.complete.action", type = "test.complete")
    public Parameter testComplete(@VariablesAsType Parameter p) {
        p.info = "test.complete";
        p.data = service.getParam();
        return p;
    }

    @JobWorker(name = "test.complete.action2", type = "test.complete2")
    public Parameter testComplete2(@Variable boolean info, @VariablesAsType Parameter p) {
        p.info = info + "test.complete";
        p.data = service.getParam();
        return p;
    }

    @JobWorker(name = "test.exception.action", type = "test.exception")
    public void testException(@VariablesAsType Parameter p) {
        p.info = "test.exception";
        p.data = service.getParam();
        throw new RuntimeException("Error custom exception message");
    }

    @JobWorker(name = "test.fail.action", type = "test.fail", autoComplete = false)
    public void testFail(JobClient client, ActivatedJob job, @VariablesAsType Parameter p) {
        p.info = "test.fail";
        p.data = service.getParam();
        client.newFailCommand(job).retries(job.getRetries() - 1)
                .errorMessage("Fail custom error message").send().join();
    }

    @JobWorker(name = "test.throw.action", type = "test.throw", autoComplete = false)
    public void testThrow(JobClient client, ActivatedJob job, @VariablesAsType Parameter p) {
        p.info = "test.throw";
        p.data = service.getParam();
        client.newThrowErrorCommand(job).errorCode("throw custom error code")
                .errorMessage("throw custom error message")
                .send().join();
    }

}
