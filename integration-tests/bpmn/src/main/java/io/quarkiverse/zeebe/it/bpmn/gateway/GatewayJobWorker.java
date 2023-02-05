package io.quarkiverse.zeebe.it.bpmn.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkiverse.zeebe.JobWorker;
import io.quarkiverse.zeebe.VariablesAsType;

public class GatewayJobWorker {

    private static final Logger log = LoggerFactory.getLogger(GatewayJobWorker.class);

    @JobWorker(type = "gateway-empty-data")
    public Parameter emptyData(@VariablesAsType Parameter p) {
        p.data = null;
        p.info = "empty data";
        return p;
    }

    @JobWorker(type = "gateway-read-data")
    public Parameter readData(@VariablesAsType Input input) {
        if (input.read) {
            Parameter p = new Parameter();
            p.data = "read data";
            return p;
        }
        return null;
    }

    @JobWorker(type = "gateway-show-data")
    public void showData(@VariablesAsType Parameter p) {
        log.info("Parameter {} / {}", p.data, p.info);
    }

    @JobWorker(type = "gateway-update-data")
    public Parameter updateData(@VariablesAsType Parameter p) {
        p.data = "update[" + p.data + "]";
        p.info = "update data";
        return p;
    }
}
