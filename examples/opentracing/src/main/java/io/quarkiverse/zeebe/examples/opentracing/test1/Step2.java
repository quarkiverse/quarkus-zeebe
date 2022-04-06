package io.quarkiverse.zeebe.examples.opentracing.test1;

import javax.inject.Inject;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeTraced;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeTraced
@ZeebeWorker(type = "step2")
public class Step2 implements JobHandler {

    @Inject
    Step2Service service;

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        Parameter p = job.getVariablesAsType(Parameter.class);
        p.info = "step2";
        p.data = service.getParam();
        //        client.newFailCommand(job).retries(job.getRetries() - 1).errorMessage("error message").send().join();
        //        client.newThrowErrorCommand(job).errorCode("1234343").errorMessage("error message").send().join();
        throw new RuntimeException("Error exception");
    }
}
