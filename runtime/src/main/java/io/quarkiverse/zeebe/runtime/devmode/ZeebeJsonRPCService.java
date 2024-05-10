package io.quarkiverse.zeebe.runtime.devmode;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import io.camunda.zeebe.protocol.record.value.*;
import io.camunda.zeebe.protocol.record.value.deployment.Process;
import io.quarkiverse.zeebe.runtime.ZeebeClientService;
import io.quarkiverse.zeebe.runtime.devmode.store.RecordStore;
import io.quarkiverse.zeebe.runtime.devmode.store.RecordStoreItem;
import io.quarkus.arc.Arc;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Multi;

public class ZeebeJsonRPCService {

    public PublishMessageResponse sendMessage(String name, String correlationKey, String duration,
            Map<String, Object> variables) {

        var tmp = getClient().newPublishMessageCommand().messageName(name);
        PublishMessageCommandStep1.PublishMessageCommandStep3 step3;
        if (correlationKey == null) {
            step3 = tmp.withoutCorrelationKey();
        } else {
            step3 = tmp.correlationKey(correlationKey);
        }
        return step3.variables(variables).timeToLive(Duration.parse(duration)).send().join();
    }

    public ProcessInstanceEvent createProcessInstance(Long processDefinitionKey, Map<String, Object> variables) {
        return getClient().newCreateInstanceCommand()
                .processDefinitionKey(processDefinitionKey).variables(variables)
                .send().join();
    };

    private ZeebeClient getClient() {
        return Arc.container().instance(ZeebeClientService.class).get().client();
    }

    @NonBlocking
    public Collection<RecordStoreItem<ErrorRecordValue>> errors() {
        return RecordStore.ERRORS.values();
    }

    @NonBlocking
    public Collection<RecordStoreItem<IncidentRecordValue>> incidents() {
        return RecordStore.INCIDENTS.values();
    }

    @NonBlocking
    public Collection<RecordStoreItem<JobRecordValue>> jobs() {
        return RecordStore.JOBS.values();
    }

    @NonBlocking
    public Multi<RecordStore.NotificationEvent> notifications() {
        return RecordStore.NOTIFICATIONS;
    }

    @NonBlocking
    public Collection<RecordStoreItem<ProcessInstanceRecordValue>> instances() {
        return RecordStore.INSTANCES.values();
    }

    @NonBlocking
    public InstanceWrapper instance(long id) {

        String xml = null;

        var item = RecordStore.INSTANCES.get(id);
        if (item != null) {
            var tmp = RecordStore.PROCESS_DEFINITIONS_XML.get(item.record().getValue().getProcessDefinitionKey());
            if (tmp != null) {
                xml = new String(tmp.record().getValue().getResource());
            }

        }

        return new InstanceWrapper(item, xml, new Diagram(null));
    }

    @NonBlocking
    public Collection<RecordStoreItem<Process>> processes() {
        return RecordStore.PROCESS_DEFINITIONS.values();
    }

    @NonBlocking
    public ProcessWrapper process(long id) {

        List<RecordStoreItem<ProcessInstanceRecordValue>> instances = null;
        List<RecordStoreItem<MessageStartEventSubscriptionRecordValue>> messages = null;
        List<RecordStoreItem<SignalSubscriptionRecordValue>> signals = null;
        List<RecordStoreItem<TimerRecordValue>> timers = null;

        Map<String, Map<String, Long>> elements = null;
        String xml = null;

        var item = RecordStore.PROCESS_DEFINITIONS.get(id);
        if (item != null) {
            elements = RecordStore.findProcessElements((Long) item.id());
            var tmp = RecordStore.PROCESS_DEFINITIONS_XML.get(id);
            if (tmp != null) {
                xml = new String(tmp.record().getValue().getResource());
            }

            instances = RecordStore.INSTANCES.findBy(x -> x.getValue().getProcessDefinitionKey() == item.record().getKey())
                    .toList();

            messages = RecordStore.START_EVENT_SUBSCRIPTIONS
                    .findBy(x -> x.getValue().getProcessDefinitionKey() == item.record().getKey())
                    .toList();

            signals = RecordStore.SIGNAL_SUBSCRIPTIONS
                    .findBy(x -> x.getValue().getProcessDefinitionKey() == item.record().getKey())
                    .toList();

            timers = RecordStore.TIMERS.findBy(x -> x.getValue().getProcessDefinitionKey() == item.record().getKey())
                    .filter(x -> x.record().getValue().getProcessInstanceKey() <= 0)
                    .toList();

        }

        return new ProcessWrapper(item, xml, new Diagram(elements), instances, messages, signals, timers);
    }

    @NonBlocking
    public String xml(long id) {
        return new String(RecordStore.PROCESS_DEFINITIONS_XML.get(id).record().getValue().getResource());
    }

    public record InstanceWrapper(RecordStoreItem<ProcessInstanceRecordValue> item,
            String xml,
            Diagram diagram) {
    }

    public record ProcessWrapper(RecordStoreItem<Process> item, String xml, Diagram diagram,
            List<RecordStoreItem<ProcessInstanceRecordValue>> instances,
            List<RecordStoreItem<MessageStartEventSubscriptionRecordValue>> messages,
            List<RecordStoreItem<SignalSubscriptionRecordValue>> signals,
            List<RecordStoreItem<TimerRecordValue>> timers) {
    }

    public record Diagram(Map<String, Map<String, Long>> elements) {
    }
}
