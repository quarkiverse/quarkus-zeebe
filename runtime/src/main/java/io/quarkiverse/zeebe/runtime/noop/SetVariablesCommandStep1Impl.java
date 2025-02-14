package io.quarkiverse.zeebe.runtime.noop;

import java.io.InputStream;
import java.util.Map;

import io.camunda.zeebe.client.api.command.SetVariablesCommandStep1;
import io.camunda.zeebe.client.api.response.SetVariablesResponse;
import io.camunda.zeebe.client.impl.response.SetVariablesResponseImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class SetVariablesCommandStep1Impl extends AbstractStep<SetVariablesResponse>
        implements SetVariablesCommandStep1, SetVariablesCommandStep1.SetVariablesCommandStep2 {

    @Override
    public SetVariablesCommandStep2 variables(InputStream variables) {
        return this;
    }

    @Override
    public SetVariablesCommandStep2 variables(String variables) {
        return this;
    }

    @Override
    public SetVariablesCommandStep2 variables(Map<String, Object> variables) {
        return this;
    }

    @Override
    public SetVariablesCommandStep2 variables(Object variables) {
        return this;
    }

    @Override
    public SetVariablesCommandStep2 local(boolean local) {
        return this;
    }

    @Override
    protected SetVariablesResponse create() {
        return new SetVariablesResponseImpl(GatewayOuterClass.SetVariablesResponse.getDefaultInstance());
    }

    @Override
    public SetVariablesCommandStep1 useRest() {
        return this;
    }

    @Override
    public SetVariablesCommandStep1 useGrpc() {
        return this;
    }

    @Override
    public SetVariablesCommandStep2 operationReference(long operationReference) {
        return this;
    }
}
