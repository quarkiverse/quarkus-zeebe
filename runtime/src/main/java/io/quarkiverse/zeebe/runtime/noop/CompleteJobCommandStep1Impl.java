package io.quarkiverse.zeebe.runtime.noop;

import java.io.InputStream;
import java.util.Map;

import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.response.CompleteJobResponse;
import io.camunda.zeebe.client.impl.response.CompleteJobResponseImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class CompleteJobCommandStep1Impl extends AbstractStep<CompleteJobResponse> implements CompleteJobCommandStep1 {
    @Override
    public CompleteJobCommandStep1 variables(InputStream variables) {
        return this;
    }

    @Override
    public CompleteJobCommandStep1 variables(String variables) {
        return this;
    }

    @Override
    public CompleteJobCommandStep1 variables(Map<String, Object> variables) {
        return this;
    }

    @Override
    public CompleteJobCommandStep1 variables(Object variables) {
        return this;
    }

    @Override
    public CompleteJobCommandStep1 variable(String key, Object value) {
        return this;
    }

    @Override
    protected CompleteJobResponse create() {
        return new CompleteJobResponseImpl(GatewayOuterClass.CompleteJobResponse.getDefaultInstance());
    }
}
