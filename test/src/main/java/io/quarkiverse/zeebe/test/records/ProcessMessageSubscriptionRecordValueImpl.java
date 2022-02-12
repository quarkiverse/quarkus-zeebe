package io.quarkiverse.zeebe.test.records;

import java.util.Map;

import io.camunda.zeebe.protocol.record.value.ProcessMessageSubscriptionRecordValue;
import io.quarkiverse.zeebe.test.ProtoBufUtil;
import io.zeebe.exporter.proto.Schema;

public class ProcessMessageSubscriptionRecordValueImpl extends RecordValueImpl
        implements ProcessMessageSubscriptionRecordValue {

    private final Schema.ProcessMessageSubscriptionRecord record;

    private Map<String, Object> variables;

    public ProcessMessageSubscriptionRecordValueImpl(Schema.ProcessMessageSubscriptionRecord record) {
        this.record = record;
    }

    @Override
    public long getProcessInstanceKey() {
        return record.getProcessInstanceKey();
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
    public long getMessageKey() {
        return record.getMessageKey();
    }

    @Override
    public String getMessageName() {
        return record.getMessageName();
    }

    @Override
    public String getCorrelationKey() {
        return getCorrelationKey();
    }

    @Override
    public String getElementId() {
        return record.getElementId();
    }

    @Override
    public boolean isInterrupting() {
        return record.getIsInterrupting();
    }

    @Override
    public Map<String, Object> getVariables() {
        if (variables == null) {
            variables = ProtoBufUtil.mapObject(record.getVariables());
        }
        return variables;
    }

    @Override
    public Schema.RecordMetadata getMetadata() {
        return record.getMetadata();
    }
}
