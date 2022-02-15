package io.quarkiverse.zeebe.test.records;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.camunda.zeebe.protocol.record.value.DeploymentRecordValue;
import io.camunda.zeebe.protocol.record.value.deployment.DeploymentResource;
import io.camunda.zeebe.protocol.record.value.deployment.ProcessMetadataValue;
import io.zeebe.exporter.proto.Schema;

public class DeploymentRecordValueImpl extends RecordValueImpl implements DeploymentRecordValue {

    private final Schema.DeploymentRecord record;

    private List<ProcessMetadataValue> processMetadataValues;

    private List<DeploymentResource> resources;

    public DeploymentRecordValueImpl(Schema.DeploymentRecord record) {
        this.record = record;
    }

    @Override
    public List<DeploymentResource> getResources() {
        if (resources == null) {
            List<DeploymentResource> tmp = new ArrayList<>();
            record.getResourcesList().forEach(x -> tmp.add(new DeploymentResourceImpl(x)));
            resources = tmp;
        }
        return resources;
    }

    @Override
    public List<ProcessMetadataValue> getProcessesMetadata() {
        if (processMetadataValues == null) {
            List<ProcessMetadataValue> tmp = new ArrayList<>();
            record.getProcessMetadataList().forEach(x -> tmp.add(new ProcessMetadataValueImpl(x)));
            processMetadataValues = tmp;
        }
        return processMetadataValues;
    }

    @Override
    Schema.RecordMetadata getMetadata() {
        return record.getMetadata();
    }

    public static class DeploymentResourceImpl implements DeploymentResource {

        private final Schema.DeploymentRecord.Resource resource;

        public DeploymentResourceImpl(Schema.DeploymentRecord.Resource resource) {
            this.resource = resource;
        }

        @Override
        public byte[] getResource() {
            return resource.getResource().toByteArray();
        }

        @Override
        public String getResourceName() {
            return resource.getResourceName();
        }
    }

    public static class ProcessMetadataValueImpl extends RecordValueImpl implements ProcessMetadataValue {

        private final Schema.DeploymentRecord.ProcessMetadata record;

        public ProcessMetadataValueImpl(Schema.DeploymentRecord.ProcessMetadata record) {
            this.record = record;
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
        public Schema.RecordMetadata getMetadata() {
            return null;
        }
    }
}
