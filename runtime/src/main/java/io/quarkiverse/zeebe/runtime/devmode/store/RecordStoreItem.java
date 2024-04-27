package io.quarkiverse.zeebe.runtime.devmode.store;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;

public record RecordStoreItem<T extends RecordValue>(Object id, Record<T> record) {

}
