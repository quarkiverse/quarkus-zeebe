package io.quarkiverse.zeebe.it.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.quarkiverse.zeebe.ZeebeWorker;

@ZeebeWorker(type = "gateway-show-data")
public class ShowData implements JobHandler {

    private static final Logger log = LoggerFactory.getLogger(ShowData.class);

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        Parameter p = job.getVariablesAsType(Parameter.class);
        log.info("Parameter {} / {}", p.data, p.info);
        client.newCompleteCommand(job.getKey()).send().join();
    }
}
