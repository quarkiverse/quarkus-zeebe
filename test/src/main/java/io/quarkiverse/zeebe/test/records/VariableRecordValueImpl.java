package io.quarkiverse.zeebe.test.records;

import io.camunda.zeebe.protocol.record.value.VariableRecordValue;
import io.zeebe.exporter.proto.Schema;

public class VariableRecordValueImpl extends RecordValueImpl implements VariableRecordValue {

    private final Schema.VariableRecord record;

    public VariableRecordValueImpl(Schema.VariableRecord record) {
        this.record = record;
    }

    @Override
    public Schema.RecordMetadata getMetadata() {
        return record.getMetadata();
    }

    @Override
    public String getName() {
        return record.getName();
    }

    @Override
    public String getValue() {
        return record.getValue();
    }

    @Override
    public long getScopeKey() {
        return record.getScopeKey();
    }

    @Override
    public long getProcessInstanceKey() {
        return record.getProcessInstanceKey();
    }

    @Override
    public long getProcessDefinitionKey() {
        return record.getProcessDefinitionKey();
    }
}
