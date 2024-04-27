package io.quarkiverse.zeebe.runtime.devmode.service;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;

public class RecordStoreItem<T extends RecordValue> {

    private final Record<T> record;

    private final Object id;

    public RecordStoreItem(Object id, Record<T> record) {
        this.record = record;
        this.id = id;
    }

    public Record<T> getRecord() {
        return record;
    }

    public Object getId() {
        return id;
    }
}
