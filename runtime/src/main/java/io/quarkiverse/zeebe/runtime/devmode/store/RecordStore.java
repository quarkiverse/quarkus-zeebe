package io.quarkiverse.zeebe.runtime.devmode.store;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import io.camunda.zeebe.protocol.Protocol;
import io.camunda.zeebe.protocol.record.ImmutableRecord;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.intent.MessageIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.*;
import io.camunda.zeebe.protocol.record.value.deployment.ImmutableProcess;
import io.camunda.zeebe.protocol.record.value.deployment.Process;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

public class RecordStore {

    public static final BroadcastProcessor<NotificationEvent> NOTIFICATIONS = BroadcastProcessor
            .create();

    public static final Store<ProcessInstanceRecordValue> ELEMENT_INSTANCES = Store
            .create();

    public static final Store<ProcessInstanceRecordValue> INSTANCES = Store.create();

    public static final Store<Process> PROCESS_DEFINITIONS = Store.create();

    public static final Store<Process> PROCESS_DEFINITIONS_XML = Store.create();

    public static final Store<JobRecordValue> JOBS = Store.create();

    public static final Store<JobRecordValue> USER_TASKS = Store.create();

    public static final Store<VariableRecordValue> VARIABLES = Store.create();

    public static final Store<ErrorRecordValue> ERRORS = Store.create();

    public static final Store<TimerRecordValue> TIMERS = Store.create();

    public static final Store<SignalRecordValue> SIGNALS = Store.create();

    public static final Store<MessageRecordValue> MESSAGES = Store.create();

    public static final Store<IncidentRecordValue> INCIDENTS = Store.create();

    public static final Store<SignalSubscriptionRecordValue> SIGNAL_SUBSCRIPTIONS = Store.create();

    public static final Store<EscalationRecordValue> ESCALATIONS = Store.create();

    public static final Store<ProcessMessageSubscriptionRecordValue> PROCESS_MESSAGE_SUBSCRIPTIONS = Store.create();

    public static final Store<MessageStartEventSubscriptionRecordValue> START_EVENT_SUBSCRIPTIONS = Store.create();

    public static void importProcessInstance(final Record<ProcessInstanceRecordValue> record) {
        if (record.getValue().getProcessInstanceKey() == record.getKey()) {
            var item = INSTANCES.put(record, r -> r.getValue().getProcessInstanceKey());

            ProcessInstanceIntent intent = (ProcessInstanceIntent) record.getIntent();
            ProcessInstanceRecordValue value = record.getValue();
            switch (intent) {
                case ELEMENT_ACTIVATED -> {
                    item.data().put("start", localDateTime(record.getTimestamp()));
                    item.data().put("state", "ACTIVE");
                    sendEvent(
                            new InstanceEvent(value.getProcessInstanceKey(), value.getProcessDefinitionKey(),
                                    RecordStore.ProcessInstanceEventType.CREATED));
                }
                case ELEMENT_TERMINATED, ELEMENT_COMPLETED -> {
                    if (intent == ProcessInstanceIntent.ELEMENT_COMPLETED) {
                        item.data().put("state", "COMPLETED");
                    } else {
                        item.data().put("state", "TERMINATED");
                    }
                    item.data().put("end", localDateTime(record.getTimestamp()));
                    sendEvent(
                            new InstanceEvent(value.getProcessInstanceKey(), value.getProcessDefinitionKey(),
                                    ProcessInstanceEventType.REMOVED));
                }
            }
        }
        ELEMENT_INSTANCES.putIfAbsent(record, r -> r.getPartitionId() + "-" + r.getPosition());
    }

    public static void importProcess(final Record<Process> record) {
        if (record.getPartitionId() != Protocol.DEPLOYMENT_PARTITION) {
            return;
        }

        var e = PROCESS_DEFINITIONS.get(record.getValue().getProcessDefinitionKey());
        if (e != null) {
            //TODO: check version and duplicates
            return;
        }

        var ic = (ImmutableRecord<Process>) record;
        var p = (ImmutableProcess) record.getValue();

        var item = PROCESS_DEFINITIONS.put(ic.withValue(p.withResource()), r -> r.getValue().getProcessDefinitionKey());
        item.data().put("time", localDateTime(record.getTimestamp()));

        PROCESS_DEFINITIONS_XML.put(record, r -> r.getValue().getProcessDefinitionKey());

        sendEvent(new ProcessEvent(ProcessEventType.DEPLOYED));
    }

    public static void importJob(final Record<JobRecordValue> record) {
        JobRecordValue value = record.getValue();
        if (UserTaskHeaders.JOB_TYPE.equals(value.getType())) {
            USER_TASKS.put(record, Record::getKey);
            return;
        }
        JOBS.put(record, Record::getKey);
    }

    public static void importVariable(final Record<VariableRecordValue> record) {
        var absent = VARIABLES.putIfAbsent(record, r -> r.getPartitionId() + "#" + r.getPosition());
        if (absent) {
            VariableRecordValue value = record.getValue();
            sendEvent(
                    new InstanceEvent(value.getProcessInstanceKey(), value.getProcessDefinitionKey(),
                            ProcessInstanceEventType.UPDATED));
        }
    }

    public static void importError(final Record<ErrorRecordValue> record) {
        ERRORS.putIfAbsent(record, Record::getPosition);
    }

    public static void importTimer(final Record<TimerRecordValue> record) {
        TIMERS.put(record, Record::getKey);
    }

    public static void importSignal(final Record<SignalRecordValue> record) {
        SIGNALS.putIfAbsent(record, Record::getKey);
    }

    public static void importMessage(final Record<MessageRecordValue> record) {
        var msg = MESSAGES.get(record.getKey());
        if (msg != null) {
            MessageIntent intent = (MessageIntent) record.getIntent();
            if (MessageIntent.PUBLISHED == intent) {
                return;
            }
        }
        MESSAGES.put(record, Record::getKey);
    }

    public static void importIncident(final Record<IncidentRecordValue> record) {
        INCIDENTS.put(record, Record::getKey);
    }

    public static void importSignalSubscription(final Record<SignalSubscriptionRecordValue> record) {
        SIGNAL_SUBSCRIPTIONS.putIfAbsent(record, Record::getKey);
    }

    public static void importEscalation(final Record<EscalationRecordValue> record) {
        ESCALATIONS.putIfAbsent(record, Record::getKey);
    }

    public static void importMessageSubscription(final Record<ProcessMessageSubscriptionRecordValue> record) {
        PROCESS_MESSAGE_SUBSCRIPTIONS.put(record,
                r -> r.getValue().getElementInstanceKey() + "#" + r.getValue().getMessageName());
    }

    public static void importMessageStartEventSubscription(final Record<MessageStartEventSubscriptionRecordValue> record) {
        START_EVENT_SUBSCRIPTIONS.put(record,
                r -> r.getValue().getProcessDefinitionKey() + "#" + r.getValue().getMessageName());
    }

    private static void sendEvent(ProcessEvent data) {
        sendEvent(new NotificationEvent(NotificationEventType.PROCESS, data));
    }

    private static void sendEvent(InstanceEvent data) {
        sendEvent(new NotificationEvent(NotificationEventType.PROCESS_INSTANCE, data));
    }

    private static void sendEvent(ClusterEvent data) {
        sendEvent(new NotificationEvent(NotificationEventType.CLUSTER, data));
    }

    private static void sendEvent(NotificationEvent data) {
        NOTIFICATIONS.onNext(data);
    }

    public record ProcessEvent(ProcessEventType type) {
    }

    public enum ProcessEventType {
        DEPLOYED;
    }

    public record ClusterEvent(String message, ClusterEventType type) {
    }

    public enum ClusterEventType {
        ERROR;
    }

    public record NotificationEvent(NotificationEventType type, Object data) {
    }

    public enum NotificationEventType {
        PROCESS,
        PROCESS_INSTANCE,
        CLUSTER;
    }

    public record InstanceEvent(long processInstanceKey, long processDefinitionKey, ProcessInstanceEventType type) {
    }

    public enum ProcessInstanceEventType {
        UPDATED,
        CREATED,
        REMOVED;
    }

    private static String localDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.systemDefault()).toString();
    }

}
