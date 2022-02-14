package io.quarkiverse.zeebe.test.records;

import java.util.Map;

import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.quarkiverse.zeebe.test.ProtoBufUtil;
import io.zeebe.exporter.proto.Schema;

public class JobRecordValueImpl extends RecordValueImpl implements JobRecordValue {

    private final Schema.JobRecord record;

    private Map<String, String> headers;

    private Map<String, Object> variables;

    public JobRecordValueImpl(Schema.JobRecord record) {
        this.record = record;
    }

    @Override
    public String getType() {
        return record.getType();
    }

    @Override
    public Map<String, String> getCustomHeaders() {
        if (headers == null) {
            headers = ProtoBufUtil.mapString(record.getCustomHeaders());
        }
        return headers;
    }

    @Override
    public String getWorker() {
        return record.getWorker();
    }

    @Override
    public int getRetries() {
        return record.getRetries();
    }

    @Override
    public long getRetryBackoff() {
        return 0;
    }

    @Override
    public long getRecurringTime() {
        return 0;
    }

    @Override
    public long getDeadline() {
        return record.getDeadline();
    }

    @Override
    public String getErrorMessage() {
        return record.getErrorMessage();
    }

    @Override
    public String getErrorCode() {
        return null;
    }

    @Override
    public String getElementId() {
        return record.getElementId();
    }

    @Override
    public long getElementInstanceKey() {
        return record.getElementInstanceKey();
    }

    @Override
    public String getBpmnProcessId() {
        return record.getBpmnProcessId();
    }

    @Override
    public int getProcessDefinitionVersion() {
        return record.getWorkflowDefinitionVersion();
    }

    @Override
    public long getProcessDefinitionKey() {
        return record.getProcessDefinitionKey();
    }

    @Override
    public Map<String, Object> getVariables() {
        if (variables == null) {
            variables = ProtoBufUtil.mapObject(record.getVariables());
        }
        return variables;
    }

    @Override
    public long getProcessInstanceKey() {
        return record.getProcessInstanceKey();
    }

    @Override
    Schema.RecordMetadata getMetadata() {
        return record.getMetadata();
    }

}
