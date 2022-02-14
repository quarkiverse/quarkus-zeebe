package io.quarkiverse.zeebe.test.records;

import io.camunda.zeebe.protocol.record.value.TimerRecordValue;
import io.zeebe.exporter.proto.Schema;

public class TimerRecordValueImpl extends RecordValueImpl implements TimerRecordValue {

    private final Schema.TimerRecord record;

    public TimerRecordValueImpl(Schema.TimerRecord record) {
        this.record = record;
    }

    @Override
    public long getProcessDefinitionKey() {
        return record.getProcessDefinitionKey();
    }

    @Override
    public long getElementInstanceKey() {
        return record.getElementInstanceKey();
    }

    @Override
    public long getProcessInstanceKey() {
        return record.getProcessInstanceKey();
    }

    @Override
    public long getDueDate() {
        return record.getDueDate();
    }

    @Override
    public String getTargetElementId() {
        return record.getTargetElementId();
    }

    @Override
    public int getRepetitions() {
        return record.getRepetitions();
    }

    @Override
    Schema.RecordMetadata getMetadata() {
        return record.getMetadata();
    }
}
