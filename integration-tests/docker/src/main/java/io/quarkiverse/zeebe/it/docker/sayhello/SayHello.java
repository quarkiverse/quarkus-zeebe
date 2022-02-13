package io.quarkiverse.zeebe.it.docker.sayhello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeWorker(type = "hello_task")
public class SayHello implements JobHandler {

    private static final Logger log = LoggerFactory.getLogger(SayHello.class);

    @Override
    public void handle(JobClient client, ActivatedJob job) {
        log.info("Job: {}", job);
        SayHelloParameter p = job.getVariablesAsType(SayHelloParameter.class);
        log.info("Parameter: {}", p);
        p.message = "Hello " + p.name;
        client.newCompleteCommand(job.getKey()).variables(p).send().join();
    }
}
