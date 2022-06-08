package io.quarkiverse.zeebe.test.records;

import io.camunda.zeebe.protocol.record.*;
import io.camunda.zeebe.protocol.record.intent.Intent;

public class RecordImpl<T extends RecordValue> implements Record<T> {

    private final RecordValueImpl recordValue;

    private final T record;

    public RecordImpl(RecordValueImpl recordValue, T record) {
        this.recordValue = recordValue;
        this.record = record;
    }

    @Override
    public long getPosition() {
        return recordValue.getMetadata().getPosition();
    }

    @Override
    public long getSourceRecordPosition() {
        return recordValue.getMetadata().getSourceRecordPosition();
    }

    @Override
    public long getKey() {
        return recordValue.getMetadata().getKey();
    }

    @Override
    public long getTimestamp() {
        return recordValue.getMetadata().getTimestamp();
    }

    @Override
    public Intent getIntent() {
        return Intent.fromProtocolValue(getValueType(), recordValue.getMetadata().getIntent());
    }

    @Override
    public int getPartitionId() {
        return recordValue.getMetadata().getPartitionId();
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.valueOf(recordValue.getMetadata().getRecordType().name());
    }

    @Override
    public RejectionType getRejectionType() {
        return RejectionType.valueOf(recordValue.getMetadata().getRejectionType());
    }

    @Override
    public String getRejectionReason() {
        return recordValue.getMetadata().getRejectionReason();
    }

    @Override
    public String getBrokerVersion() {
        return null;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.valueOf(recordValue.getMetadata().getValueType().name());
    }

    @Override
    public T getValue() {
        return record;
    }

    @Override
    public Record<T> clone() {
        return this;
    }

}
