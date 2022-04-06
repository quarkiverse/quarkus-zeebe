package io.quarkiverse.zeebe.opentelemetry;

import java.util.Map;

import io.camunda.zeebe.client.api.response.ActivatedJob;

public class MethodRequest {
    private final String clazz;
    private final ActivatedJob job;
    private final Map<String, Object> variables;

    public MethodRequest(final String clazz, final ActivatedJob job) {
        this.clazz = clazz;
        this.job = job;
        this.variables = job.getVariablesAsMap();
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getClazz() {
        return clazz;
    }

    public ActivatedJob getJob() {
        return job;
    }
}
