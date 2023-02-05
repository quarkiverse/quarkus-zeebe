package io.quarkiverse.zeebe.jobworker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkiverse.zeebe.JobWorker;
import io.quarkiverse.zeebe.VariablesAsType;
import io.quarkiverse.zeebe.ZeebeBpmnError;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class TestJobWorker {

    @JobWorker(name = "test.complete.action4", type = "test.complete4")
    public Map<String, Object> testComplete4(@VariablesAsType Parameter p) {
        p.info = "test.complete";
        return Map.of("info", p.info, "data", p.data);
    }

    @JobWorker(name = "test.complete.action41", type = "test.complete41")
    public HashMap<String, Object> testComplete41(@VariablesAsType Parameter p) {
        p.info = "test.complete";
        return new HashMap<>();
    }

    @JobWorker(name = "test.complete.action5", type = "test.complete5")
    public String testComplete5(@VariablesAsType Parameter p) {
        p.info = "test.complete";
        return "{}";
    }

    @JobWorker(name = "test.complete.action6", type = "test.complete6")
    public InputStream testComplete6(@VariablesAsType Parameter p) {
        p.info = "test.complete";
        return new ByteArrayInputStream(new byte[] { 1, 2 });
    }

    @JobWorker(name = "test.complete.action61", type = "test.complete61")
    public ByteArrayInputStream testComplete61(@VariablesAsType Parameter p) {
        p.info = "test.complete";
        return new ByteArrayInputStream(new byte[] { 1, 2 });
    }

    @JobWorker(name = "test.complete.action", type = "test.complete")
    public CompletionStage<Parameter> testComplete3(@VariablesAsType Parameter p) {
        p.info = "test.complete";
        return CompletableFuture.completedStage(p);
    }

    @JobWorker(name = "test.complete.action2", type = "test.complete2")
    public Uni<Parameter> testComplete2(@VariablesAsType Parameter p) {
        p.info = "test.complete";
        return Uni.createFrom().item(p);
    }

    @JobWorker(name = "test.complete.action3", type = "test.complete3")
    public Parameter testComplete(@VariablesAsType Parameter p) {
        p.info = "test.complete";
        return p;
    }

    @JobWorker(name = "test.exception.action", type = "test.exception")
    public void testException(@VariablesAsType Parameter p) {
        p.info = "test.exception";
        throw new RuntimeException("Error custom exception message");
    }

    @JobWorker(name = "test.fail.action", type = "test.fail", autoComplete = false)
    public void testFail(JobClient client, ActivatedJob job, @VariablesAsType Parameter p) {
        p.info = "test.fail";
        client.newFailCommand(job).retries(job.getRetries() - 1)
                .errorMessage("Fail custom error message").send().join();
    }

    @JobWorker(name = "test.throw.action", type = "test.throw", autoComplete = false)
    public void testThrow(JobClient client, ActivatedJob job, @VariablesAsType Parameter p) {
        p.info = "test.throw";
        client.newThrowErrorCommand(job).errorCode("throw custom error code")
                .errorMessage("throw custom error message")
                .send().join();
    }

    @JobWorker(name = "test.throw.action2", type = "test.throw2", autoComplete = false)
    public void testThrow2(@VariablesAsType Parameter p) throws ZeebeBpmnError {
        p.info = "test.throw";
        throw new ZeebeBpmnError("throw custom error code", "throw custom error message");
    }

}
