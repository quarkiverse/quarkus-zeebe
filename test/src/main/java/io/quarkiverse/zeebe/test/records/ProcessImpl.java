package io.quarkiverse.zeebe.test.records;

import java.nio.charset.StandardCharsets;

import io.camunda.zeebe.protocol.record.value.deployment.Process;
import io.zeebe.exporter.proto.Schema;

public class ProcessImpl extends RecordValueImpl implements Process {

    private final Schema.ProcessRecord record;

    public ProcessImpl(Schema.ProcessRecord record) {
        this.record = record;
    }

    @Override
    public byte[] getResource() {
        return record.getResource().toByteArray();
    }

    @Override
    public String getBpmnProcessId() {
        return record.getBpmnProcessId();
    }

    @Override
    public int getVersion() {
        return record.getVersion();
    }

    @Override
    public long getProcessDefinitionKey() {
        return record.getProcessDefinitionKey();
    }

    @Override
    public String getResourceName() {
        return record.getResourceName();
    }

    @Override
    public byte[] getChecksum() {
        return record.getChecksum().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean isDuplicate() {
        return false;
    }

    @Override
    Schema.RecordMetadata getMetadata() {
        return record.getMetadata();
    }
}
