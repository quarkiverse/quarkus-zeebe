package io.quarkiverse.zeebe.runtime.devmode;

import java.util.Collection;

import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;
import io.camunda.zeebe.protocol.record.value.deployment.Process;
import io.quarkiverse.zeebe.runtime.devmode.store.RecordStore;
import io.quarkiverse.zeebe.runtime.devmode.store.RecordStoreItem;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Multi;

public class MonitorJsonRPCService {

    public Multi<RecordStore.NotificationEvent> notifications() {
        return RecordStore.NOTIFICATIONS;
    }

    @NonBlocking
    public Collection<RecordStoreItem<ProcessInstanceRecordValue>> instances() {
        return RecordStore.INSTANCES.values();
    }

    @NonBlocking
    public Collection<RecordStoreItem<Process>> processes() {
        return RecordStore.PROCESS_DEFINITIONS.values();
    }
}
