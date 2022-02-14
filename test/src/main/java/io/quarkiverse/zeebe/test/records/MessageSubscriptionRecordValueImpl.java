package io.quarkiverse.zeebe.test.records;

import java.util.Map;

import io.camunda.zeebe.protocol.record.value.MessageSubscriptionRecordValue;
import io.quarkiverse.zeebe.test.ProtoBufUtil;
import io.zeebe.exporter.proto.Schema;

public class MessageSubscriptionRecordValueImpl extends RecordValueImpl implements MessageSubscriptionRecordValue {

    private final Schema.MessageSubscriptionRecord record;

    private Map<String, Object> variables;

    public MessageSubscriptionRecordValueImpl(Schema.MessageSubscriptionRecord record) {
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
    public String getMessageName() {
        return record.getMessageName();
    }

    @Override
    public String getCorrelationKey() {
        return record.getCorrelationKey();
    }

    @Override
    public long getMessageKey() {
        return record.getMessageKey();
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
    Schema.RecordMetadata getMetadata() {
        return record.getMetadata();
    }
}
