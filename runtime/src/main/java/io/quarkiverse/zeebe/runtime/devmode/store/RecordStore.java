package io.quarkiverse.zeebe.runtime.devmode.store;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.camunda.zeebe.protocol.Protocol;
import io.camunda.zeebe.protocol.record.ImmutableRecord;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.camunda.zeebe.protocol.record.intent.Intent;
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

        ELEMENT_INSTANCES.putIfAbsent(record, r -> r.getPartitionId() + "-" + r.getPosition());
        RecordStore.ProcessInstanceEventType type = ProcessInstanceEventType.UPDATED;
        ProcessInstanceIntent intent = (ProcessInstanceIntent) record.getIntent();

        if (record.getValue().getProcessInstanceKey() == record.getKey()) {
            var item = INSTANCES.put(record, r -> r.getValue().getProcessInstanceKey());

            switch (intent) {
                case ELEMENT_ACTIVATED -> {
                    item.data().put("start", localDateTime(record.getTimestamp()));
                    item.data().put("end", "");
                    item.data().put("state", "ACTIVE");
                    type = RecordStore.ProcessInstanceEventType.CREATED;

                    // update process definitions
                    var pd = PROCESS_DEFINITIONS.get(record.getValue().getProcessDefinitionKey());
                    if (pd != null) {
                        pd.data().merge("active", 0, (v, n) -> ((int) v) + 1);
                        pd.data().putIfAbsent("ended", 0);
                    }
                }
                case ELEMENT_TERMINATED, ELEMENT_COMPLETED -> {
                    if (intent == ProcessInstanceIntent.ELEMENT_COMPLETED) {
                        item.data().put("state", "COMPLETED");
                    } else {
                        item.data().put("state", "TERMINATED");
                    }
                    item.data().put("end", localDateTime(record.getTimestamp()));

                    // update process definitions
                    var pd = PROCESS_DEFINITIONS.get(record.getValue().getProcessDefinitionKey());
                    if (pd != null) {
                        pd.data().merge("active", 0, (v, n) -> ((int) v) - 1);
                        pd.data().merge("ended", 0, (v, n) -> ((int) v) + 1);
                    }
                    type = ProcessInstanceEventType.ENDED;
                }
            }
        }

        if (intent == ProcessInstanceIntent.ELEMENT_ACTIVATED || intent == ProcessInstanceIntent.ELEMENT_TERMINATED
                || intent == ProcessInstanceIntent.ELEMENT_COMPLETED) {

            ProcessInstanceRecordValue value = record.getValue();
            sendEvent(new InstanceEvent(value.getProcessInstanceKey(), value.getProcessDefinitionKey(), type));
        }
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
        item.data().put("active", 0);
        item.data().put("ended", 0);

        PROCESS_DEFINITIONS_XML.put(record, r -> r.getValue().getProcessDefinitionKey());

        sendEvent(new ProcessEvent(ProcessEventType.DEPLOYED));
    }

    public static void importJob(final Record<JobRecordValue> record) {
        JobRecordValue value = record.getValue();
        if (UserTaskHeaders.JOB_TYPE.equals(value.getType())) {
            var ut = USER_TASKS.put(record, Record::getKey);
            ut.data().put("time", localDateTime(record.getTimestamp()));
            return;
        }
        var job = JOBS.put(record, Record::getKey);
        job.data().put("time", localDateTime(record.getTimestamp()));
    }

    public static void importVariable(final Record<VariableRecordValue> record) {
        var variable = VARIABLES.putIfAbsent(record, r -> r.getPartitionId() + "#" + r.getPosition());
        if (variable != null) {
            variable.data().put("time", localDateTime(record.getTimestamp()));

            VariableRecordValue value = record.getValue();
            sendEvent(
                    new InstanceEvent(value.getProcessInstanceKey(), value.getProcessDefinitionKey(),
                            ProcessInstanceEventType.UPDATED));
        }
    }

    public static void importError(final Record<ErrorRecordValue> record) {
        var error = ERRORS.putIfAbsent(record, Record::getPosition);
        if (error != null) {
            error.data().put("created", localDateTime(record.getTimestamp()));
        }
    }

    public static void importTimer(final Record<TimerRecordValue> record) {
        var timer = TIMERS.put(record, Record::getKey);
        timer.data().put("dueDate", localDateTime(record.getValue().getDueDate()));
        timer.data().put("time", localDateTime(record.getTimestamp()));
    }

    public static void importSignal(final Record<SignalRecordValue> record) {
        var signal = SIGNALS.putIfAbsent(record, Record::getKey);
        signal.data().put("time", localDateTime(record.getTimestamp()));
    }

    public static void importMessage(final Record<MessageRecordValue> record) {
        var msg = MESSAGES.put(record, Record::getKey);
        if (msg != null) {

            MessageIntent intent = (MessageIntent) record.getIntent();
            if (MessageIntent.PUBLISHED == intent) {
                msg.data().put("name", msg.record().getValue().getName());
                msg.data().put("messageId", msg.record().getValue().getMessageId());
                msg.data().put("correlationKey", msg.record().getValue().getCorrelationKey());
            }

            msg.data().put("time", localDateTime(record.getTimestamp()));
            var value = record.getValue();
            sendEvent(new MessageEvent(value.getName(), value.getMessageId(), value.getCorrelationKey(),
                    MessageEventType.UPDATED));
        }
    }

    public static void importIncident(final Record<IncidentRecordValue> record) {
        var incident = INCIDENTS.put(record, Record::getKey);
        IncidentIntent intent = (IncidentIntent) record.getIntent();
        if (intent == IncidentIntent.CREATED) {
            incident.data().put("created", localDateTime(record.getTimestamp()));
            incident.data().put("resolved", "");
        }
        if (intent == IncidentIntent.RESOLVED) {
            incident.data().put("resolved", localDateTime(record.getTimestamp()));
        }
    }

    public static void importSignalSubscription(final Record<SignalSubscriptionRecordValue> record) {
        var signal = SIGNAL_SUBSCRIPTIONS.putIfAbsent(record, Record::getKey);
        if (signal != null) {
            signal.data().put("time", localDateTime(record.getTimestamp()));
        }
    }

    public static void importEscalation(final Record<EscalationRecordValue> record) {
        var escalation = ESCALATIONS.putIfAbsent(record, Record::getKey);
        if (escalation != null) {
            escalation.data().put("time", localDateTime(record.getTimestamp()));
        }
    }

    public static void importMessageSubscription(final Record<ProcessMessageSubscriptionRecordValue> record) {
        var item = PROCESS_MESSAGE_SUBSCRIPTIONS.put(record,
                r -> r.getValue().getElementInstanceKey() + "#" + r.getValue().getMessageName());
        item.data().put("time", localDateTime(record.getTimestamp()));
    }

    public static void importMessageStartEventSubscription(final Record<MessageStartEventSubscriptionRecordValue> record) {
        var item = START_EVENT_SUBSCRIPTIONS.put(record,
                r -> r.getValue().getProcessDefinitionKey() + "#" + r.getValue().getMessageName());
        item.data().put("time", localDateTime(record.getTimestamp()));
    }

    private static void sendEvent(MessageEvent data) {
        sendEvent(new NotificationEvent(NotificationEventType.MESSAGE, data));
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
        MESSAGE,
        PROCESS,
        PROCESS_INSTANCE,
        CLUSTER;
    }

    public record InstanceEvent(long processInstanceKey, long processDefinitionKey, ProcessInstanceEventType type) {
    }

    public enum ProcessInstanceEventType {
        UPDATED,
        CREATED,
        ENDED;
    }

    public enum MessageEventType {
        UPDATED,
    }

    public record MessageEvent(String name, String messageId, String correlationKey, MessageEventType type) {
    }

    private static String localDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.systemDefault()).toString();
    }

    static Set<BpmnElementType> PROCESS_ELEMENTS_TYPES = Set.of(BpmnElementType.PROCESS, BpmnElementType.MULTI_INSTANCE_BODY);
    static Set<Intent> PROCESS_ELEMENT_INTENTS = Set.of(ProcessInstanceIntent.ELEMENT_ACTIVATED,
            ProcessInstanceIntent.ELEMENT_COMPLETED);

    public static Map<String, Map<String, Long>> findProcessElements(Long pdk) {
        return ELEMENT_INSTANCES.findBy(
                record -> record.getValue().getProcessDefinitionKey() == pdk
                        && !PROCESS_ELEMENTS_TYPES.contains(record.getValue().getBpmnElementType())
                        && PROCESS_ELEMENT_INTENTS.contains(record.getIntent()))
                .collect(
                        Collectors.groupingBy(
                                x -> x.record().getValue().getElementId(),
                                Collectors.mapping(
                                        x -> x.record().getIntent(),
                                        Collectors.groupingBy(Intent::name, Collectors.counting()))));
    }
}
