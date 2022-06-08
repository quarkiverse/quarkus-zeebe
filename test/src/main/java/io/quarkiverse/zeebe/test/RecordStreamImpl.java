package io.quarkiverse.zeebe.test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.quarkiverse.zeebe.test.records.*;
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast;

public class RecordStreamImpl implements AutoCloseable {

    public static ZeebeHazelcast zeebeHazelcast;

    private final List<Record<?>> records = new CopyOnWriteArrayList<>();

    private final RecordStream recordStream;

    public RecordStreamImpl(String address) {

        recordStream = RecordStream.of(() -> records);

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress(address);
        HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

        zeebeHazelcast = ZeebeHazelcast.newBuilder(hz)
                .readFromHead()
                .addDeploymentListener(r -> add(new DeploymentRecordValueImpl(r)))
                .addProcessInstanceListener(r -> add(new ProcessInstanceRecordValueImpl(r)))
                .addVariableListener(r -> add(new VariableRecordValueImpl(r)))
                .addJobListener(r -> add(new JobRecordValueImpl(r)))
                .addMessageListener(r -> add(new MessageRecordValueImpl(r)))
                .addJobBatchListener(r -> add(new JobBatchRecordValueImpl(r)))
                .addProcessListener(r -> add(new ProcessImpl(r)))
                .addVariableDocumentListener(r -> add(new VariableDocumentRecordValueImpl(r)))
                .addIncidentListener(r -> add(new IncidentRecordValueImpl(r)))
                .addTimerListener(r -> add(new TimerRecordValueImpl(r)))
                .addMessageSubscriptionListener(r -> add(new MessageSubscriptionRecordValueImpl(r)))
                .addMessageStartEventSubscriptionListener(
                        r -> add(new MessageStartEventSubscriptionRecordValueImpl(r)))
                .addProcessMessageSubscriptionListener(
                        r -> add(new ProcessMessageSubscriptionRecordValueImpl(r)))
                .build();
    }

    public RecordStream getRecordStream() {
        return recordStream;
    }

    public void close() throws Exception {
        if (zeebeHazelcast != null) {
            zeebeHazelcast.close();
        }
    }

    private <T extends RecordValue> void add(T value) {
        RecordImpl<T> record = new RecordImpl<>((RecordValueImpl) value, value);
        records.add(record);
    }

}
