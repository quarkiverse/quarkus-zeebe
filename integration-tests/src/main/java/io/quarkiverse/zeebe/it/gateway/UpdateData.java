package io.quarkiverse.zeebe.it.gateway;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeWorker(type = "gateway-update-data")
public class UpdateData implements JobHandler {

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        Parameter p = job.getVariablesAsType(Parameter.class);
        p.data = "update[" + p.data + "]";
        p.info = "update data";

        client.newCompleteCommand(job.getKey()).variables(p).send().join();
    }
}
