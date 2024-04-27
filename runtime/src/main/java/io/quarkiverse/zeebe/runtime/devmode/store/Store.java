package io.quarkiverse.zeebe.runtime.devmode.store;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class Store2<RECORD extends RecordValue> {

    private final Map<Object, RecordStoreItem<RECORD>> data = new TreeMap<>();

    public static <RECORD extends RecordValue> Store2<RECORD> create() {
        return new Store2<>();
    }

    public boolean putIfAbsent(Record<RECORD> record, Function<Record<RECORD>, Object> f) {
        var id = f.apply(record);
        var item = get(id);
        if (item != null) {
            return false;
        }
        put(id, record);
        return true;
    }

    public RecordStoreItem<RECORD> put(Record<RECORD> record, Function<Record<RECORD>, Object> f) {
        var id = f.apply(record);
        return put(id, record);
    }

    private RecordStoreItem<RECORD> put(Object id, Record<RECORD> record) {
        var item = new RecordStoreItem<>(id, record);
        data.put(item.getId(), item);
        return item;
    }

    public Collection<RecordStoreItem<RECORD>> values() {
        return data.values();
    }

    public RecordStoreItem<RECORD> get(Object id) {
        return data.get(id);
    }

}
