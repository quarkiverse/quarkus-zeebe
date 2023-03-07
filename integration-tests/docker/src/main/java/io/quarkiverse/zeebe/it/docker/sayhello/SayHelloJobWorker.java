package io.quarkiverse.zeebe.it.docker.sayhello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkiverse.zeebe.JobWorker;
import io.quarkiverse.zeebe.VariablesAsType;

public class SayHelloJobWorker {

    private static final Logger log = LoggerFactory.getLogger(SayHelloJobWorker.class);

    @JobWorker(type = "hello_task")
    public SayHelloParameter sayHello(JobClient client, ActivatedJob job, @VariablesAsType SayHelloParameter p) {
        log.info("Job: {}, Parameter: {}", job, p);
        p.message = "Hello " + p.name;
        return p;
    }
}
