package io.quarkiverse.zeebe.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import io.camunda.zeebe.process.test.testengine.RecordStreamSource;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.value.*;
import io.camunda.zeebe.protocol.record.value.deployment.Process;
import io.camunda.zeebe.test.util.record.CompactRecordLogger;
import io.quarkiverse.zeebe.test.records.*;
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast;

public class RecordStreamSourceImpl implements RecordStreamSource, AutoCloseable {

    public static ZeebeHazelcast zeebeHazelcast;

    private final List<Record<?>> records = new ArrayList<>();

    private final List<Record<ProcessInstanceRecordValue>> processInstances = new CopyOnWriteArrayList<>();

    private final List<Record<VariableRecordValue>> variables = new CopyOnWriteArrayList<>();

    private final List<Record<DeploymentRecordValue>> deployments = new CopyOnWriteArrayList<>();

    private final List<Record<JobRecordValue>> jobs = new CopyOnWriteArrayList<>();

    private final List<Record<JobBatchRecordValue>> jobBatches = new CopyOnWriteArrayList<>();

    private final List<Record<Process>> processes = new CopyOnWriteArrayList<>();

    private final List<Record<MessageRecordValue>> messages = new CopyOnWriteArrayList<>();

    private final List<Record<VariableDocumentRecordValue>> variableDocuments = new CopyOnWriteArrayList<>();

    private final List<Record<IncidentRecordValue>> incidents = new CopyOnWriteArrayList<>();

    private final List<Record<TimerRecordValue>> timers = new CopyOnWriteArrayList<>();

    private final List<Record<MessageSubscriptionRecordValue>> messageSubscriptions = new CopyOnWriteArrayList<>();

    private final List<Record<MessageStartEventSubscriptionRecordValue>> messageStartEventSubscriptions = new CopyOnWriteArrayList<>();

    private final List<Record<ProcessMessageSubscriptionRecordValue>> processMessageSubscriptions = new CopyOnWriteArrayList<>();

    public RecordStreamSourceImpl(String address) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress(address);
        HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

        zeebeHazelcast = ZeebeHazelcast.newBuilder(hz)
                .readFromHead()
                .addDeploymentListener(r -> add(deployments, new DeploymentRecordValueImpl(r)))
                .addProcessInstanceListener(r -> add(processInstances, new ProcessInstanceRecordValueImpl(r)))
                .addVariableListener(r -> add(variables, new VariableRecordValueImpl(r)))
                .addJobListener(r -> add(jobs, new JobRecordValueImpl(r)))
                .addMessageListener(r -> add(messages, new MessageRecordValueImpl(r)))
                .addJobBatchListener(r -> add(jobBatches, new JobBatchRecordValueImpl(r)))
                .addProcessListener(r -> add(processes, new ProcessImpl(r)))
                .addVariableDocumentListener(r -> add(variableDocuments, new VariableDocumentRecordValueImpl(r)))
                .addIncidentListener(r -> add(incidents, new IncidentRecordValueImpl(r)))
                .addTimerListener(r -> add(timers, new TimerRecordValueImpl(r)))
                .addMessageSubscriptionListener(r -> add(messageSubscriptions, new MessageSubscriptionRecordValueImpl(r)))
                .addMessageStartEventSubscriptionListener(
                        r -> add(messageStartEventSubscriptions, new MessageStartEventSubscriptionRecordValueImpl(r)))
                .addProcessMessageSubscriptionListener(
                        r -> add(processMessageSubscriptions, new ProcessMessageSubscriptionRecordValueImpl(r)))
                .build();
    }

    public void close() throws Exception {
        if (zeebeHazelcast != null) {
            zeebeHazelcast.close();
        }
    }

    private <T extends RecordValue> void add(List<Record<T>> items, T value) {
        RecordImpl<T> record = new RecordImpl<T>((RecordValueImpl) value, value);
        items.add(record);
        records.add(record);
    }

    @Override
    public Iterable<Record<?>> records() {
        return Collections.unmodifiableList(records);
    }

    @Override
    public Iterable<Record<ProcessInstanceRecordValue>> processInstanceRecords() {
        return Collections.unmodifiableList(processInstances);
    }

    @Override
    public Iterable<Record<JobRecordValue>> jobRecords() {
        return Collections.unmodifiableList(jobs);
    }

    @Override
    public Iterable<Record<JobBatchRecordValue>> jobBatchRecords() {
        return Collections.unmodifiableList(jobBatches);
    }

    @Override
    public Iterable<Record<DeploymentRecordValue>> deploymentRecords() {
        return Collections.unmodifiableList(deployments);
    }

    @Override
    public Iterable<Record<Process>> processRecords() {
        return Collections.unmodifiableList(processes);
    }

    @Override
    public Iterable<Record<VariableRecordValue>> variableRecords() {
        return Collections.unmodifiableList(variables);
    }

    @Override
    public Iterable<Record<VariableDocumentRecordValue>> variableDocumentRecords() {
        return Collections.unmodifiableList(variableDocuments);
    }

    @Override
    public Iterable<Record<IncidentRecordValue>> incidentRecords() {
        return Collections.unmodifiableList(incidents);
    }

    @Override
    public Iterable<Record<TimerRecordValue>> timerRecords() {
        return Collections.unmodifiableList(timers);
    }

    @Override
    public Iterable<Record<MessageRecordValue>> messageRecords() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public Iterable<Record<MessageSubscriptionRecordValue>> messageSubscriptionRecords() {
        return Collections.unmodifiableList(messageSubscriptions);
    }

    @Override
    public Iterable<Record<MessageStartEventSubscriptionRecordValue>> messageStartEventSubscriptionRecords() {
        return Collections.unmodifiableList(messageStartEventSubscriptions);
    }

    @Override
    public Iterable<Record<ProcessMessageSubscriptionRecordValue>> processMessageSubscriptionRecords() {
        return Collections.unmodifiableList(processMessageSubscriptions);
    }

    @Override
    public void print(boolean compact) {
        final List<Record<?>> recordsList = new ArrayList<>();
        records().forEach(recordsList::add);

        if (compact) {
            new CompactRecordLogger(recordsList).log();
        } else {
            System.out.println("===== records (count: ${count()}) =====");
            recordsList.forEach(record -> System.out.println(record.toJson()));
            System.out.println("---------------------------");
        }
    }
}
