package io.quarkiverse.zeebe.examples.opentelemetry;

import jakarta.inject.Inject;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkiverse.zeebe.JobWorker;
import io.quarkiverse.zeebe.VariablesAsType;

import java.util.Map;

public class TestJobWorker {

    @Inject
    TestService service;

    @JobWorker(name = "open-telemetry-step1", type = "open-telemetry-step1")
    public Object openTelemetryStep1(ActivatedJob job) {
        return Map.of("step1", true);
    }

    @JobWorker(name = "open-telemetry-step2", type = "open-telemetry-step2")
    public Object openTelemetryStep2() {
        return Map.of("step1", true);
    }

    @JobWorker(name = "test.complete.action", type = "test.complete")
    public Parameter testComplete(@VariablesAsType Parameter p) {
        p.info = "test.complete";
        p.data = service.getParam();
        return p;
    }

    @JobWorker(name = "test.exception.action", type = "test.exception")
    public void testException(ActivatedJob job) {
        Parameter p = job.getVariablesAsType(Parameter.class);
        p.info = "test.exception";
        p.data = service.getParam();
        throw new RuntimeException("Error custom exception message");
    }

    @JobWorker(name = "test.fail.action", type = "test.fail", autoComplete = false)
    public void testFail(JobClient client, ActivatedJob job) {
        Parameter p = job.getVariablesAsType(Parameter.class);
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
