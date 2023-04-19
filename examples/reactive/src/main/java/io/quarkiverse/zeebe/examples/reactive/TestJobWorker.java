package io.quarkiverse.zeebe.examples.reactive;

import java.util.concurrent.CompletionStage;

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkiverse.zeebe.JobWorker;
import io.smallrye.mutiny.Uni;

public class TestJobWorker {

    private static final Logger log = LoggerFactory.getLogger(TestJobWorker.class);

    @Inject
    JobCounter counter;

    @Inject
    @RestClient
    TestRestClient restClient;

    private Uni<Void> test() {
        log.info("No waiting ...");
        return Uni.createFrom().voidItem();
    }

    @JobWorker(type = "completionStage-auto")
    public CompletionStage<Void> noneBlockAutoRestCall(final JobClient client, final ActivatedJob job) {
        counter.init();
        log.info("Invoke REST call...");
        return restClient.completionStage().thenApply(d -> {
            counter.inc();
            return null;
        });
    }

    @JobWorker(type = "uni-auto")
    public Uni<Void> uni(final JobClient client, final ActivatedJob job) {
        counter.init();
        log.info("Invoke REST call...");
        return test().onItem().call(() -> restClient.uni()).replaceWith(() -> {
            counter.inc();
            return null;
        });
    }

    @JobWorker(type = "blocking-auto")
    public void blockingAutoRestCall(final JobClient client, final ActivatedJob job) {
        counter.init();
        log.info("Invoke REST call...");
        String tmp = restClient.blocking();
        counter.inc();
    }

    @JobWorker(type = "blocking", autoComplete = false)
    public void blocking(final JobClient client, final ActivatedJob job) {
        counter.init();
        log.info("Invoke REST call...");
        String tmp = restClient.blocking();
        client.newCompleteCommand(job.getKey()).send().join();
        counter.inc();
    }

}
