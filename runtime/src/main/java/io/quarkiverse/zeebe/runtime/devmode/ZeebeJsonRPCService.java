package io.quarkiverse.zeebe.runtime.devmode;

import java.util.Collection;

import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;
import io.camunda.zeebe.protocol.record.value.deployment.Process;
import io.quarkiverse.zeebe.runtime.devmode.store.RecordStore;
import io.quarkiverse.zeebe.runtime.devmode.store.RecordStoreItem;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Multi;

public class ZeebeJsonRPCService {

    public Multi<RecordStore.NotificationEvent> notifications() {
        return RecordStore.NOTIFICATIONS;
    }

    @NonBlocking
    public Collection<RecordStoreItem<ProcessInstanceRecordValue>> instances() {
        return RecordStore.INSTANCES.values();
    }

    @NonBlocking
    public RecordStoreItem<ProcessInstanceRecordValue> instance(long id) {
        return RecordStore.INSTANCES.get(id);
    }

    @NonBlocking
    public Collection<RecordStoreItem<Process>> processes() {
        return RecordStore.PROCESS_DEFINITIONS.values();
    }

    @NonBlocking
    public RecordStoreItem<Process> process(long id) {
        return RecordStore.PROCESS_DEFINITIONS.get(id);
    }

    @NonBlocking
    public String xml(long id) {
        return new String(RecordStore.PROCESS_DEFINITIONS_XML.get(id).record().getValue().getResource());
    }

}
