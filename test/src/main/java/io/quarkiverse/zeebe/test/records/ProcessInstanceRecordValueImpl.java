package io.quarkiverse.zeebe.test.records;

import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;
import io.zeebe.exporter.proto.Schema;

public class ProcessInstanceRecordValueImpl extends RecordValueImpl implements ProcessInstanceRecordValue {

    private final Schema.ProcessInstanceRecord record;

    public ProcessInstanceRecordValueImpl(Schema.ProcessInstanceRecord record) {
        this.record = record;
    }

    @Override
    Schema.RecordMetadata getMetadata() {
        return record.getMetadata();
    }

    @Override
    public String getBpmnProcessId() {
        return record.getBpmnProcessId();
    }

    @Override
    public int getVersion() {
        return record.getVersion();
    }

    @Override
    public long getProcessDefinitionKey() {
        return record.getProcessDefinitionKey();
    }

    @Override
    public long getProcessInstanceKey() {
        return record.getProcessInstanceKey();
    }

    @Override
    public String getElementId() {
        return record.getElementId();
    }

    @Override
    public long getFlowScopeKey() {
        return record.getFlowScopeKey();
    }

    @Override
    public BpmnElementType getBpmnElementType() {
        return BpmnElementType.valueOf(record.getBpmnElementType());
    }

    @Override
    public long getParentProcessInstanceKey() {
        return record.getParentProcessInstanceKey();
    }

    @Override
    public long getParentElementInstanceKey() {
        return record.getParentElementInstanceKey();
    }

}
