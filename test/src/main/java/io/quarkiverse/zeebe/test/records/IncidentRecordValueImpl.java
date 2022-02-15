package io.quarkiverse.zeebe.test.records;

import io.camunda.zeebe.protocol.record.value.ErrorType;
import io.camunda.zeebe.protocol.record.value.IncidentRecordValue;
import io.zeebe.exporter.proto.Schema;

public class IncidentRecordValueImpl extends RecordValueImpl implements IncidentRecordValue {

    private final Schema.IncidentRecord record;

    public IncidentRecordValueImpl(Schema.IncidentRecord record) {
        this.record = record;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.valueOf(record.getErrorType());
    }

    @Override
    public String getErrorMessage() {
        return record.getErrorMessage();
    }

    @Override
    public String getBpmnProcessId() {
        return record.getBpmnProcessId();
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
    public long getElementInstanceKey() {
        return record.getElementInstanceKey();
    }

    @Override
    public long getJobKey() {
        return record.getJobKey();
    }

    @Override
    public long getVariableScopeKey() {
        return record.getVariableScopeKey();
    }

    @Override
    Schema.RecordMetadata getMetadata() {
        return record.getMetadata();
    }
}
