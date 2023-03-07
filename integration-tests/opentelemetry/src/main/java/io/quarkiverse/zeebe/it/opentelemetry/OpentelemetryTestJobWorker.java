package io.quarkiverse.zeebe.it.opentelemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.quarkiverse.zeebe.JobWorker;
import io.quarkiverse.zeebe.Variable;
import io.quarkiverse.zeebe.VariablesAsType;

public class OpentelemetryTestJobWorker {

    private static final Logger log = LoggerFactory.getLogger(OpentelemetryTestJobWorker.class);

    @JobWorker(type = "test")
    public Parameter openTelemetryTestMethod(ActivatedJob job, @VariablesAsType Parameter p, @Variable String name,
            @Variable String message) {
        log.info("Job: {}, Parameter: {}, name: {}, message: {}", job, p, name, message);
        p.message = "Ok";
        return p;
    }

}
