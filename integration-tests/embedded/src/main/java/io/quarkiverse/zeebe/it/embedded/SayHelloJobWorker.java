package io.quarkiverse.zeebe.it.embedded;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.quarkiverse.zeebe.JobWorker;
import io.quarkiverse.zeebe.VariablesAsType;

public class SayHelloJobWorker {

    private static final Logger log = LoggerFactory.getLogger(SayHelloJobWorker.class);

    @Inject
    SayHelloBusinessService service;

    @JobWorker(type = "hello_task")
    public SayHelloParameter sayHello(ActivatedJob job, @VariablesAsType SayHelloParameter p) {
        log.info("Job: {}, Parameter: {}", job, p);
        p.message = service.hello(p.name);
        return p;
    }
}
