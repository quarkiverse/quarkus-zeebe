package io.quarkiverse.zeebe.examples.opentelemetry.test1;

import javax.inject.Inject;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeWorker(name = "process.step1", type = "step1")
public class Step1 implements JobHandler {

    @Inject
    Step1Service service;

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        System.out.println("### " + job.getVariablesAsMap());
        Parameter p = job.getVariablesAsType(Parameter.class);
        p.info = "step1";
        p.data = service.getParam();
        client.newCompleteCommand(job.getKey()).variables(p).send().join();
    }
}
