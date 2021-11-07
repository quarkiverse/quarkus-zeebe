package io.quarkiverse.zeebe.it;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

public class ProcessInstanceEventImpl implements ProcessInstanceEvent {
    public long processDefinitionKey;
    public String bpmnProcessId;
    public int version;
    public long processInstanceKey;

    @Override
    public long getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    @Override
    public String getBpmnProcessId() {
        return bpmnProcessId;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public long getProcessInstanceKey() {
        return processInstanceKey;
    }
}
