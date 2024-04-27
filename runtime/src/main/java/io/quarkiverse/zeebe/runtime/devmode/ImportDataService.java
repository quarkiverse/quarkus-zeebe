package io.quarkiverse.zeebe.runtime.devmode;

import io.camunda.zeebe.protocol.Protocol;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.intent.MessageIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.*;
import io.camunda.zeebe.protocol.record.value.deployment.Process;
import io.quarkiverse.zeebe.runtime.devmode.store.UserTaskHeaders;
import io.quarkiverse.zeebe.runtime.devmode.store.*;

public class ImportDataService {

    private static final Store<ProcessInstanceRecordValue> ELEMENT_INSTANCES = Store
            .create();

    private static final Store<ProcessInstanceRecordValue> INSTANCES = Store.create();

    private static final Store<Process> PROCESS_DEFINITIONS = Store.create();

    private static final Store<JobRecordValue> JOBS = Store.create();

    private static final Store<JobRecordValue> USER_TASKS = Store.create();

    private static final Store<VariableRecordValue> VARIABLES = Store.create();

    private static final Store<ErrorRecordValue> ERRORS = Store.create();

    private static final Store<TimerRecordValue> TIMERS = Store.create();

    private static final Store<SignalRecordValue> SIGNALS = Store.create();

    private static final Store<MessageRecordValue> MESSAGES = Store.create();

    private static final Store<IncidentRecordValue> INCIDENTS = Store.create();

    private static final Store<SignalSubscriptionRecordValue> SIGNAL_SUBSCRIPTIONS = Store.create();

    private static final Store<EscalationRecordValue> ESCALATIONS = Store.create();

    private static final Store<ProcessMessageSubscriptionRecordValue> PROCESS_MESSAGE_SUBSCRIPTIONS = Store.create();

    private static final Store<MessageStartEventSubscriptionRecordValue> START_EVENT_SUBSCRIPTIONS = Store.create();

    NotificationService notificationService = new NotificationService();

    public void importProcessInstance(final Record<ProcessInstanceRecordValue> record) {
        if (record.getValue().getProcessInstanceKey() == record.getKey()) {
            INSTANCES.put(record, r -> r.getValue().getProcessInstanceKey());
            ProcessInstanceIntent intent = (ProcessInstanceIntent) record.getIntent();
            ProcessInstanceRecordValue value = record.getValue();
            switch (intent) {
                case ELEMENT_ACTIVATED -> notificationService.sendEvent(
                        new NotificationService.InstanceEvent(value.getProcessInstanceKey(), value.getProcessDefinitionKey(),
                                NotificationService.ProcessInstanceEventType.CREATED));
                case ELEMENT_TERMINATED, ELEMENT_COMPLETED -> notificationService.sendEvent(
                        new NotificationService.InstanceEvent(value.getProcessInstanceKey(), value.getProcessDefinitionKey(),
                                NotificationService.ProcessInstanceEventType.REMOVED));
            }
        }
        ELEMENT_INSTANCES.putIfAbsent(record, r -> r.getPartitionId() + "-" + r.getPosition());
    }

    public void importProcess(final Record<Process> record) {
        if (record.getPartitionId() != Protocol.DEPLOYMENT_PARTITION) {
            return;
        }

        var absent = PROCESS_DEFINITIONS.putIfAbsent(record, r -> r.getValue().getProcessDefinitionKey());
        if (!absent) {
            //TODO: check version and duplicates
            return;
        }

        notificationService.sendEvent(new NotificationService.ProcessEvent(NotificationService.ProcessEventType.DEPLOYED));
    }

    public void importJob(final Record<JobRecordValue> record) {
        JobRecordValue value = record.getValue();
        if (UserTaskHeaders.JOB_TYPE.equals(value.getType())) {
            USER_TASKS.put(record, Record::getKey);
            return;
        }
        JOBS.put(record, Record::getKey);
    }

    public void importVariable(final Record<VariableRecordValue> record) {
        var absent = VARIABLES.putIfAbsent(record, r -> r.getPartitionId() + "#" + r.getPosition());
        if (absent) {
            VariableRecordValue value = record.getValue();
            notificationService.sendEvent(
                    new NotificationService.InstanceEvent(value.getProcessInstanceKey(), value.getProcessDefinitionKey(),
                            NotificationService.ProcessInstanceEventType.UPDATED));
        }
    }

    public void importError(final Record<ErrorRecordValue> record) {
        ERRORS.putIfAbsent(record, Record::getPosition);
    }

    public void importTimer(final Record<TimerRecordValue> record) {
        TIMERS.put(record, Record::getKey);
    }

    public void importSignal(final Record<SignalRecordValue> record) {
        SIGNALS.putIfAbsent(record, Record::getKey);
    }

    public void importMessage(final Record<MessageRecordValue> record) {
        var msg = MESSAGES.get(record.getKey());
        if (msg != null) {
            MessageIntent intent = (MessageIntent) record.getIntent();
            if (MessageIntent.PUBLISHED == intent) {
                return;
            }
        }
        MESSAGES.put(record, Record::getKey);
    }

    public void importIncident(final Record<IncidentRecordValue> record) {
        INCIDENTS.put(record, Record::getKey);
    }

    public void importSignalSubscription(final Record<SignalSubscriptionRecordValue> record) {
        SIGNAL_SUBSCRIPTIONS.putIfAbsent(record, Record::getKey);
    }

    public void importEscalation(final Record<EscalationRecordValue> record) {
        ESCALATIONS.putIfAbsent(record, Record::getKey);
    }

    public void importMessageSubscription(final Record<ProcessMessageSubscriptionRecordValue> record) {
        PROCESS_MESSAGE_SUBSCRIPTIONS.put(record, r->r.getValue().getElementInstanceKey() + "#" + r.getValue().getMessageName());
    }

    public void importMessageStartEventSubscription(final Record<MessageStartEventSubscriptionRecordValue> record) {
        START_EVENT_SUBSCRIPTIONS.put(record, r->r.getValue().getProcessDefinitionKey() + "#" + r.getValue().getMessageName());
    }
}
