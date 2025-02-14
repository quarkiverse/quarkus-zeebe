package io.quarkiverse.zeebe.runtime.noop;

import java.io.InputStream;
import java.util.Map;

import io.camunda.zeebe.client.api.command.ModifyProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.response.ModifyProcessInstanceResponse;
import io.camunda.zeebe.client.impl.response.ModifyProcessInstanceResponseImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class ModifyProcessInstanceCommandStep1Impl extends AbstractStep<ModifyProcessInstanceResponse>
        implements ModifyProcessInstanceCommandStep1.ModifyProcessInstanceCommandStep2, ModifyProcessInstanceCommandStep1,
        ModifyProcessInstanceCommandStep1.ModifyProcessInstanceCommandStep3 {

    @Override
    public ModifyProcessInstanceCommandStep3 activateElement(String elementId) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 activateElement(String elementId, long ancestorElementInstanceKey) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep2 terminateElement(long elementInstanceKey) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 withVariables(InputStream variables) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 withVariables(InputStream variables, String scopeId) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 withVariables(String variables) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 withVariables(String variables, String scopeId) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 withVariables(Map<String, Object> variables) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 withVariables(Map<String, Object> variables, String scopeId) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 withVariables(Object variables) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 withVariables(Object variables, String scopeId) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 withVariable(String key, Object value) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep3 withVariable(String key, Object value, String scopeId) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep1 and() {
        return new ModifyProcessInstanceCommandStep1Impl();
    }

    @Override
    protected ModifyProcessInstanceResponse create() {
        return new ModifyProcessInstanceResponseImpl(GatewayOuterClass.ModifyProcessInstanceResponse.getDefaultInstance());
    }

    @Override
    public ModifyProcessInstanceCommandStep2 operationReference(long operationReference) {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep1 useRest() {
        return this;
    }

    @Override
    public ModifyProcessInstanceCommandStep1 useGrpc() {
        return this;
    }
}
