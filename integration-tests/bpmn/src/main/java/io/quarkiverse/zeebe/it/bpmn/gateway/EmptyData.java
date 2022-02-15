package io.quarkiverse.zeebe.it.bpmn.gateway;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeWorker(type = "gateway-empty-data")
public class EmptyData implements JobHandler {

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        Parameter p = job.getVariablesAsType(Parameter.class);
        p.data = null;
        p.info = "empty data";
        client.newCompleteCommand(job.getKey()).variables(p).send().join();
    }
}
