package io.quarkiverse.zeebe.runtime.noop;

import java.io.InputStream;
import java.util.Map;

import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.impl.response.CreateProcessInstanceResponseImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class CreateProcessInstanceCommandStep1Impl extends AbstractStep<ProcessInstanceEvent>
        implements CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3, CreateProcessInstanceCommandStep1,
        CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep2 {

    @Override
    public CreateProcessInstanceCommandStep2 bpmnProcessId(String bpmnProcessId) {
        return this;
    }

    @Override
    public CreateProcessInstanceCommandStep3 processDefinitionKey(long processDefinitionKey) {
        return this;
    }

    @Override
    public CreateProcessInstanceCommandStep3 version(int version) {
        return this;
    }

    @Override
    public CreateProcessInstanceCommandStep3 latestVersion() {
        return this;
    }

    @Override
    public CreateProcessInstanceCommandStep3 variables(InputStream variables) {
        return this;
    }

    @Override
    public CreateProcessInstanceCommandStep3 variables(String variables) {
        return this;
    }

    @Override
    public CreateProcessInstanceCommandStep3 variables(Map<String, Object> variables) {
        return this;
    }

    @Override
    public CreateProcessInstanceCommandStep3 variables(Object variables) {
        return this;
    }

    @Override
    public CreateProcessInstanceCommandStep3 variable(String key, Object value) {
        return this;
    }

    @Override
    public CreateProcessInstanceCommandStep3 startBeforeElement(String elementId) {
        return this;
    }

    @Override
    public CreateProcessInstanceWithResultCommandStep1 withResult() {
        return null;
    }

    @Override
    public CreateProcessInstanceCommandStep3 tenantId(String tenantId) {
        return this;
    }

    @Override
    public ProcessInstanceEvent create() {
        return new CreateProcessInstanceResponseImpl(GatewayOuterClass.CreateProcessInstanceResponse.getDefaultInstance());
    }
}
