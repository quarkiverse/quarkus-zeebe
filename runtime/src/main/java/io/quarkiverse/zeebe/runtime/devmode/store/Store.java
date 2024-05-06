package io.quarkiverse.zeebe.runtime.devmode.store;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;

public class Store<RECORD extends RecordValue> {

    private final Map<Object, RecordStoreItem<RECORD>> data = new TreeMap<>(Collections.reverseOrder());

    public static <KEY, RECORD extends RecordValue> Store<RECORD> create() {
        return new Store<>();
    }

    public RecordStoreItem<RECORD> putIfAbsent(Record<RECORD> record, Function<Record<RECORD>, Object> f) {
        var id = f.apply(record);
        var item = get(id);
        if (item != null) {
            return null;
        }
        return put(id, record);
    }

    public RecordStoreItem<RECORD> put(Record<RECORD> record, Function<Record<RECORD>, Object> f) {
        var id = f.apply(record);
        return put(id, record);
    }

    private RecordStoreItem<RECORD> put(Object id, Record<RECORD> record) {
        var item = createItem(id, record);
        var old = data.put(item.id(), item);
        if (old != null && !old.data().isEmpty()) {
            item.data().putAll(old.data());
        }
        return item;
    }

    public Collection<RecordStoreItem<RECORD>> values() {
        return data.values();
    }

    public RecordStoreItem<RECORD> get(Object id) {
        return data.get(id);
    }

    public Optional<RecordStoreItem<RECORD>> findFirstBy(Predicate<Record<RECORD>> filter) {
        return data.values().stream().filter(x -> filter.test(x.record())).findFirst();
    }

    public Stream<RecordStoreItem<RECORD>> findBy(Predicate<Record<RECORD>> filter) {
        return data.values().stream().filter(x -> filter.test(x.record()));
    }

    protected RecordStoreItem<RECORD> createItem(Object id, Record<RECORD> record) {
        return new RecordStoreItem<>(id, record, new HashMap<>());
    }
}
