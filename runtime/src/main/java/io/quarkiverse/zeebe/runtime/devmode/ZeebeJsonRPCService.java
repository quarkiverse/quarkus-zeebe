package io.quarkiverse.zeebe.runtime.devmode;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import io.camunda.zeebe.client.api.response.*;
import io.camunda.zeebe.model.bpmn.instance.FlowElement;
import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.*;
import io.camunda.zeebe.protocol.record.value.deployment.Process;
import io.quarkiverse.zeebe.runtime.ZeebeClientService;
import io.quarkiverse.zeebe.runtime.devmode.store.BpmnModel;
import io.quarkiverse.zeebe.runtime.devmode.store.RecordStore;
import io.quarkiverse.zeebe.runtime.devmode.store.RecordStoreItem;
import io.quarkus.arc.Arc;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Multi;

public class ZeebeJsonRPCService {

    public Object jobComplete(long key, Map<String, Object> variables) {
        getClient().newCompleteCommand(key).variables(variables).send().join();
        return Map.of("command", "jobComplete", "key", key);
    }

    public Object jobFail(long key, int retries, String errorMessage, Map<String, Object> variables) {
        getClient().newFailCommand(key).retries(retries).errorMessage(errorMessage)
                .variables(variables).send().join();
        return Map.of("command", "jobFail", "key", key);
    }

    public Object jobThrowError(long key, String errorCode, String errorMessage, Map<String, Object> variables) {
        getClient().newThrowErrorCommand(key).errorCode(errorCode)
                .errorMessage(errorMessage).variables(variables).send().join();
        return Map.of("command", "jobThrowError", "key", key);
    }

    public Object jobRetries(long key, int retries) {
        getClient().newUpdateRetriesCommand(key).retries(retries).send().join();
        return Map.of("command", "jobRetries", "key", key);
    }

    public Object resolveIncident(long key, long jobKey, int retries) {
        if (jobKey > 0) {
            getClient().newUpdateRetriesCommand(jobKey).retries(retries).send().join();
        }
        getClient().newResolveIncidentCommand(key).send().join();
        return Map.of("command", "resolveIncident", "incidentKey", key);
    }

    public SetVariablesResponse setVariables(long key, boolean local, Map<String, Object> variables) {
        return getClient().newSetVariablesCommand(key).variables(variables).local(local).send().join();
    }

    public Object userTaskComplete(long key, Map<String, Object> variables) {
        getClient().newUserTaskCompleteCommand(key).variables(variables).send().join();
        return Map.of("command", "userTaskComplete", "userTaskKey", key);
    }

    public Object cancelProcessInstance(long processInstanceKey) {
        getClient().newCancelInstanceCommand(processInstanceKey).send().join();
        return Map.of("command", "cancelProcessInstance", "processInstanceKey", processInstanceKey);
    }

    public BroadcastSignalResponse sendSignal(String name, Map<String, Object> variables) {
        return getClient().newBroadcastSignalCommand()
                .signalName(name)
                .variables(variables)
                .send().join();
    }

    public DeploymentEvent deployProcess(String name, String xml) {
        return getClient().newDeployResourceCommand()
                .addResourceStringUtf8(xml, name)
                .send().join();
    }

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
    public Collection<RecordStoreItem<JobRecordValue>> userTasks() {
        return RecordStore.USER_TASKS.values();
    }

    @NonBlocking
    public Collection<RecordStoreItem<SignalRecordValue>> signals() {
        return RecordStore.SIGNALS.values();
    }

    @NonBlocking
    public Collection<RecordStoreItem<MessageRecordValue>> messages() {
        return RecordStore.MESSAGES.values();
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
    public Multi<RecordStore.Notification> notifications() {
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

    @NonBlocking
    public ProcessWrapper process(long id) {

        List<RecordStoreItem<ProcessInstanceRecordValue>> instances = null;
        List<RecordStoreItem<MessageStartEventSubscriptionRecordValue>> messages = null;
        List<RecordStoreItem<SignalSubscriptionRecordValue>> signals = null;
        List<RecordStoreItem<TimerRecordValue>> timers = null;

        Map<String, Map<String, Long>> elements = null;
        String xml = null;

        var item = RecordStore.PROCESS_DEFINITIONS.get(id);
        if (item == null) {
            return null;
        }

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

        return new ProcessWrapper(item, xml, new ProcessDiagram(elements), instances, messages, signals, timers);
    }

    @NonBlocking
    public String xml(long id) {
        return new String(RecordStore.PROCESS_DEFINITIONS_XML.get(id).record().getValue().getResource());
    }

    @NonBlocking
    public InstanceWrapper instance(long id) {

        byte[] xml = null;

        var item = RecordStore.INSTANCES.get(id);
        if (item == null) {
            return null;
        }

        var active = (item.data().get("end") == "");

        var tmp = RecordStore.PROCESS_DEFINITIONS_XML.get(item.record().getValue().getProcessDefinitionKey());
        if (tmp != null) {
            xml = tmp.record().getValue().getResource();
        }
        if (xml == null) {
            return null;
        }

        RecordStoreItem<ProcessInstanceRecordValue> parent = null;
        if (item.record().getValue().getParentProcessInstanceKey() > 0) {
            parent = RecordStore.INSTANCES.get(item.record().getValue().getParentProcessInstanceKey());
        }

        Set<String> completedActivities = new HashSet<>();
        Set<String> completedItems = new HashSet<>();
        List<String> takenSequenceFlows = new ArrayList<>();
        Map<String, Long> completedElementsById = new HashMap<>();
        Map<String, Long> enteredElementsById = new HashMap<>();
        Set<Long> completedElementInstances = new HashSet<>();

        final Map<Long, String> elementIdsForKeys = new HashMap<>();
        elementIdsForKeys.put(item.record().getValue().getProcessInstanceKey(), item.record().getValue().getBpmnProcessId());

        var events = RecordStore.ELEMENT_INSTANCES
                .findBy(x -> x.getValue().getProcessInstanceKey() == item.record().getValue().getProcessInstanceKey()).toList();
        events.forEach(e -> {
            var key = e.record().getKey();
            var elementId = e.record().getValue().getElementId();

            elementIdsForKeys.put(key, elementId);
            if (ProcessInstanceIntent.ELEMENT_COMPLETED.name().equals(e.record().getIntent().name())) {
                if (BpmnElementType.PROCESS != e.record().getValue().getBpmnElementType()) {
                    completedItems.add(elementId);
                }
            }
            if (ProcessInstanceIntent.ELEMENT_COMPLETED.name().equals(e.record().getIntent().name())
                    || ProcessInstanceIntent.ELEMENT_TERMINATED.name().equals(e.record().getIntent().name())) {

                completedElementInstances.add(key);
                if (BpmnElementType.PROCESS != e.record().getValue().getBpmnElementType()) {
                    completedActivities.add(elementId);
                }
                if (BpmnElementType.MULTI_INSTANCE_BODY != e.record().getValue().getBpmnElementType()) {
                    completedElementsById.compute(elementId, ZeebeJsonRPCService::count);
                }
            }
            if (ProcessInstanceIntent.SEQUENCE_FLOW_TAKEN.name().equals(e.record().getIntent().name())) {
                takenSequenceFlows.add(elementId);
            }
            if (ProcessInstanceIntent.ELEMENT_ACTIVATED.name().equals(e.record().getIntent().name())) {
                if (BpmnElementType.MULTI_INSTANCE_BODY != e.record().getValue().getBpmnElementType() &&
                        BpmnElementType.PROCESS != e.record().getValue().getBpmnElementType()) {
                    enteredElementsById.compute(elementId, ZeebeJsonRPCService::count);
                }
            }
        });

        List<RecordStoreItem<ProcessInstanceRecordValue>> ancestorActivities = new ArrayList<>();
        List<RecordStoreItem<ProcessInstanceRecordValue>> terminateActiveActivities = new ArrayList<>();
        final List<String> activeActivitiesTmp = events.stream()
                .filter(e -> BpmnElementType.PROCESS != e.record().getValue().getBpmnElementType())
                .filter(e -> ProcessInstanceIntent.ELEMENT_ACTIVATED.name().equals(e.record().getIntent().name()))
                .peek(ancestorActivities::add)
                .filter(e -> !completedActivities.contains(e.record().getValue().getElementId()))
                .peek(terminateActiveActivities::add)
                .map(e -> e.record().getValue().getElementId())
                .toList();

        var elementStates = enteredElementsById.entrySet().stream()
                .map(e -> {
                    long completedInstances = completedElementsById.getOrDefault(e.getKey(), 0L);
                    return new ElementInstanceState(e.getKey(), e.getValue() - completedInstances, completedInstances);
                }).toList();

        List<String> activeActivities = new ArrayList<>(activeActivitiesTmp);

        var tt = RecordStore.INCIDENTS.findBy(x -> x.getValue().getProcessInstanceKey() == id).toList();
        var incidents = tt.stream().map(
                x -> new InstanceIncident(elementIdsForKeys.getOrDefault(x.record().getValue().getElementInstanceKey(), ""), x))
                .toList();

        List<String> incidentActivities = incidents.stream()
                .filter(x -> !IncidentIntent.RESOLVED.name().equals(x.item().record().getIntent().name()))
                .map(x -> elementIdsForKeys.get(x.item.record().getValue().getElementInstanceKey()))
                .distinct().toList();

        activeActivities.removeAll(incidentActivities);

        // Activity scope
        List<ActiveScope> activeScopes = null;
        if (active) {
            activeScopes = events.stream()
                    .filter(e -> ProcessInstanceIntent.ELEMENT_ACTIVATED.name().equals(e.record().getIntent().name()))
                    .map(e -> e.record().getKey())
                    .filter(e -> !completedElementInstances.contains(e))
                    .map(k -> new ActiveScope(k, elementIdsForKeys.get(k))).toList();
        }

        var variablesRaw = RecordStore.VARIABLES.findBy(x -> x.getValue().getProcessInstanceKey() == id);
        var variablesMap = variablesRaw.collect(Collectors
                .groupingBy(e -> new VariableId(e.record().getValue().getScopeKey(), e.record().getValue().getName())));
        var variables = variablesMap.entrySet().stream()
                .map((e) -> {
                    var x = e.getValue().get(0);
                    return new Variable(elementIdsForKeys.get(e.getKey().scopeKey()),
                            x.record().getValue().getName(),
                            x.record().getValue().getScopeKey(),
                            x.record().getValue().getValue(),
                            x.data().get("time"), e.getValue());
                })
                .toList();

        var jobs = RecordStore.JOBS.findBy(x -> x.getValue().getProcessInstanceKey() == id).toList();
        var errors = RecordStore.ERRORS.findBy(x -> x.getValue().getProcessInstanceKey() == id).toList();
        var timers = RecordStore.TIMERS.findBy(x -> x.getValue().getProcessInstanceKey() == id).toList();
        var messageSubscriptions = RecordStore.PROCESS_MESSAGE_SUBSCRIPTIONS
                .findBy(x -> x.getValue().getProcessInstanceKey() == id).toList();
        var escalations = RecordStore.ESCALATIONS.findBy(x -> x.getValue().getProcessInstanceKey() == id).toList();
        var userTasks = RecordStore.USER_TASKS.findBy(x -> x.getValue().getProcessInstanceKey() == id).toList();
        var callProcessInstances = RecordStore.INSTANCES.findBy(x -> x.getValue().getParentProcessInstanceKey() == id)
                .map(x -> new CalledProcessInstance(elementIdsForKeys.get(x.record().getValue().getParentProcessInstanceKey()),
                        x))
                .toList();

        final List<ActivateElementItem> activateActivities = new ArrayList<>();
        final var bpmn = BpmnModel.loadModel(xml);
        final Map<String, String> flowElements = new HashMap<>();
        bpmn.getModelElementsByType(FlowElement.class).forEach(e -> {

            String name = Optional.ofNullable(e.getName()).orElse("");
            flowElements.put(e.getId(), name);

            BpmnElementType type = BpmnElementType.bpmnElementTypeFor(e.getElementType().getTypeName());
            if (type != null && !MODIFY_UNSUPPORTED_ELEMENT_TYPES.contains(type.name())) {
                activateActivities.add(new ActivateElementItem(e.getId(), name));
            }
        });

        var bpmnElementInfos = BpmnModel.loadBpmnElements(bpmn);

        var auditLogEntries = events.stream()
                .map(x -> new AuditLog(flowElements.getOrDefault(x.record().getValue().getElementId(), ""), x))
                .toList();

        var diagram = new Diagram(activeActivities, incidentActivities, takenSequenceFlows, completedActivities,
                bpmnElementInfos);

        return new InstanceWrapper(active, item, parent, new String(xml), diagram,
                elementStates, activeScopes, auditLogEntries, callProcessInstances, incidents,
                jobs,
                messageSubscriptions,
                timers,
                errors,
                variables,
                completedItems,
                terminateActiveActivities,
                activateActivities,
                ancestorActivities,
                escalations,
                userTasks);
    }

    public record InstanceWrapper(boolean active, RecordStoreItem<ProcessInstanceRecordValue> item,
            RecordStoreItem<ProcessInstanceRecordValue> parent,
            String xml, Diagram diagram,
            List<ElementInstanceState> elementStates, List<ActiveScope> activeScopes, List<AuditLog> auditLogEntries,
            List<CalledProcessInstance> callProcessInstances,
            List<InstanceIncident> incidents,
            List<RecordStoreItem<JobRecordValue>> jobs,
            List<RecordStoreItem<ProcessMessageSubscriptionRecordValue>> messageSubscriptions,
            List<RecordStoreItem<TimerRecordValue>> timers,
            List<RecordStoreItem<ErrorRecordValue>> errors,
            List<Variable> variables,
            Set<String> completedItems,
            List<RecordStoreItem<ProcessInstanceRecordValue>> terminateActiveActivities,
            List<ActivateElementItem> activateActivities,
            List<RecordStoreItem<ProcessInstanceRecordValue>> ancestorActivities,
            List<RecordStoreItem<EscalationRecordValue>> escalations,
            List<RecordStoreItem<JobRecordValue>> userTasks) {
    }

    public record ProcessWrapper(RecordStoreItem<Process> item, String xml, ProcessDiagram diagram,
            List<RecordStoreItem<ProcessInstanceRecordValue>> instances,
            List<RecordStoreItem<MessageStartEventSubscriptionRecordValue>> messages,
            List<RecordStoreItem<SignalSubscriptionRecordValue>> signals,
            List<RecordStoreItem<TimerRecordValue>> timers) {
    }

    public record ProcessDiagram(Map<String, Map<String, Long>> elements) {
    }

    public record Diagram(List<String> activeActivities, List<String> incidentActivities,
            List<String> takenSequenceFlows, Set<String> completedActivities,
            List<BpmnModel.BpmnElementInfo> bpmnElementInfos) {
    }

    public record ActiveScope(long value, String name) {
    }

    public record CalledProcessInstance(String elementId, RecordStoreItem<ProcessInstanceRecordValue> item) {
    }

    public record ActivateElementItem(String id, String name) {
    }

    public record AuditLog(String elementName, RecordStoreItem<ProcessInstanceRecordValue> item) {
    }

    public record InstanceIncident(String elementName, RecordStoreItem<IncidentRecordValue> item) {
    }

    public record ElementInstanceState(String elementId, long activeInstances, long endedInstances) {
    }

    public record VariableId(long scopeKey, String name) {
    }

    public record Variable(String elementId, String name, long scopeKey, String value, Object time,
            List<RecordStoreItem<VariableRecordValue>> variables) {
    }

    static Long count(String key, Long value) {
        if (value == null) {
            return 1L;
        }
        return value + 1;
    }

    private static final Set<String> MODIFY_UNSUPPORTED_ELEMENT_TYPES = Set.of(BpmnElementType.UNSPECIFIED.name(),
            BpmnElementType.START_EVENT.name(),
            BpmnElementType.SEQUENCE_FLOW.name(), BpmnElementType.BOUNDARY_EVENT.name());
}
