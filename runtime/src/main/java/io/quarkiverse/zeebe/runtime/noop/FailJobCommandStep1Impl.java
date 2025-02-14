package io.quarkiverse.zeebe.runtime.noop;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

import io.camunda.zeebe.client.api.command.FailJobCommandStep1;
import io.camunda.zeebe.client.api.response.FailJobResponse;
import io.camunda.zeebe.client.impl.response.FailJobResponseImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class FailJobCommandStep1Impl extends AbstractStep<FailJobResponse>
        implements FailJobCommandStep1.FailJobCommandStep2, FailJobCommandStep1 {

    @Override
    public FailJobCommandStep2 retries(int remainingRetries) {
        return this;
    }

    @Override
    public FailJobCommandStep2 retryBackoff(Duration backoffTimeout) {
        return this;
    }

    @Override
    public FailJobCommandStep2 errorMessage(String errorMsg) {
        return this;
    }

    @Override
    public FailJobCommandStep2 variables(InputStream variables) {
        return this;
    }

    @Override
    public FailJobCommandStep2 variables(String variables) {
        return this;
    }

    @Override
    public FailJobCommandStep2 variables(Map<String, Object> variables) {
        return this;
    }

    @Override
    public FailJobCommandStep2 variables(Object variables) {
        return this;
    }

    @Override
    public FailJobCommandStep2 variable(String key, Object value) {
        return this;
    }

    @Override
    protected FailJobResponse create() {
        return new FailJobResponseImpl(GatewayOuterClass.FailJobResponse.getDefaultInstance());
    }

    @Override
    public FailJobCommandStep1 useRest() {
        return this;
    }

    @Override
    public FailJobCommandStep1 useGrpc() {
        return this;
    }
}
