package io.quarkiverse.zeebe.it.bpmn.gateway;

import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeWorker(type = "gateway-read-data")
public class ReadData implements JobHandler {

    @Override
    public void handle(JobClient client, ActivatedJob job) {

        CompleteJobCommandStep1 cmd = client.newCompleteCommand(job.getKey());
        Input input = job.getVariablesAsType(Input.class);
        if (input.read) {
            Parameter p = new Parameter();
            p.data = "read data";
            cmd.variables(p);
        }
        cmd.send().join();
    }
}
