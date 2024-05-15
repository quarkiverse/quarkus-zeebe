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
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.*;
import io.camunda.zeebe.protocol.record.value.*;
import io.camunda.zeebe.protocol.record.value.deployment.ImmutableProcess;
import io.camunda.zeebe.protocol.record.value.deployment.Process;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

public class RecordStore {

    public static final BroadcastProcessor<Notification> NOTIFICATIONS = BroadcastProcessor
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
        NotificationType type = NotificationType.UPDATED;
        ProcessInstanceIntent intent = (ProcessInstanceIntent) record.getIntent();

        if (record.getValue().getProcessInstanceKey() == record.getKey()) {
            var item = INSTANCES.put(record, r -> r.getValue().getProcessInstanceKey());

            switch (intent) {
                case ELEMENT_ACTIVATED -> {
                    item.data().put("start", localDateTime(record.getTimestamp()));
                    item.data().put("end", "");
                    item.data().put("state", "ACTIVE");
                    type = NotificationType.CREATED;

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
                    type = NotificationType.ENDED;
                }
            }
        }

        if (intent == ProcessInstanceIntent.ELEMENT_ACTIVATED || intent == ProcessInstanceIntent.ELEMENT_TERMINATED
                || intent == ProcessInstanceIntent.ELEMENT_COMPLETED) {

            ProcessInstanceRecordValue value = record.getValue();
            sendEvent(ValueType.PROCESS_INSTANCE, type,
                    Map.of("processInstanceKey", value.getProcessInstanceKey(), "processDefinitionKey",
                            value.getProcessDefinitionKey()));
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

        sendEvent(ValueType.PROCESS, NotificationType.DEPLOYED);
    }

    public static void importJob(final Record<JobRecordValue> record) {
        JobRecordValue value = record.getValue();
        if (UserTaskHeaders.JOB_TYPE.equals(value.getType())) {
            var ut = USER_TASKS.put(record, Record::getKey);
            Map<String, String> headers = value.getCustomHeaders();
            ut.data().put("users", headers.get(UserTaskHeaders.CANDIDATE_USERS));
            ut.data().put("groups", headers.get(UserTaskHeaders.CANDIDATE_GROUPS));
            ut.data().put("assignee", headers.get(UserTaskHeaders.ASSIGNEE));
            ut.data().put("dueDate", headers.get(UserTaskHeaders.DUE_DATE));
            ut.data().put("followUpDate", headers.get(UserTaskHeaders.FOLLOW_UP_DATE));
            ut.data().put("time", localDateTime(record.getTimestamp()));
            JobIntent uti = (JobIntent) record.getIntent();
            if (uti == JobIntent.CREATED) {
                ut.data().put("created", localDateTime(record.getTimestamp()));
            }
            sendEvent(ValueType.USER_TASK, NotificationType.UPDATED);
            return;
        }
        var job = JOBS.put(record, Record::getKey);
        job.data().put("time", localDateTime(record.getTimestamp()));
        sendEvent(ValueType.JOB, NotificationType.UPDATED);

    }

    public static void importVariable(final Record<VariableRecordValue> record) {
        var variable = VARIABLES.putIfAbsent(record, r -> r.getPartitionId() + "#" + r.getPosition());
        if (variable != null) {
            variable.data().put("time", localDateTime(record.getTimestamp()));

            VariableRecordValue value = record.getValue();
            sendEvent(ValueType.VARIABLE, NotificationType.UPDATED,
                    Map.of("name", value.getName(), "processInstanceKey", value.getProcessInstanceKey(), "processDefinitionKey",
                            value.getProcessDefinitionKey()));
        }
    }

    public static void importError(final Record<ErrorRecordValue> record) {
        var error = ERRORS.putIfAbsent(record, Record::getPosition);
        if (error != null) {
            error.data().put("created", localDateTime(record.getTimestamp()));
            sendEvent(ValueType.ERROR, NotificationType.UPDATED);
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
        sendEvent(ValueType.SIGNAL, NotificationType.UPDATED, Map.of("signalName", record.getValue().getSignalName()));
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
            sendEvent(ValueType.MESSAGE, NotificationType.UPDATED,
                    Map.of("name", value.getName(), "messageId", value.getMessageId(), "correlationKey",
                            value.getCorrelationKey()));
        }
    }

    public static void importIncident(final Record<IncidentRecordValue> record) {
        var incident = INCIDENTS.put(record, Record::getKey);
        IncidentIntent intent = (IncidentIntent) record.getIntent();
        var type = NotificationType.UPDATED;
        if (intent == IncidentIntent.CREATED) {
            incident.data().put("created", localDateTime(record.getTimestamp()));
            incident.data().put("resolved", "");
            type = NotificationType.CREATED;
        }
        if (intent == IncidentIntent.RESOLVED) {
            incident.data().put("resolved", localDateTime(record.getTimestamp()));
        }
        sendEvent(ValueType.INCIDENT, type,
                Map.of("processInstanceKey", record.getValue().getProcessInstanceKey(),
                        "processDefinitionKey", record.getValue().getProcessDefinitionKey()));
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

    private static void sendEvent(ValueType event, NotificationType type) {
        NOTIFICATIONS.onNext(new Notification(event, type, Map.of()));
    }

    private static void sendEvent(ValueType event, NotificationType type, Map<String, Object> data) {
        NOTIFICATIONS.onNext(new Notification(event, type, data));
    }

    public record Notification(ValueType event, NotificationType type, Map<String, Object> data) {
    }

    public enum NotificationType {
        ERROR,
        DEPLOYED,
        UPDATED,
        CREATED,
        ENDED;
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
