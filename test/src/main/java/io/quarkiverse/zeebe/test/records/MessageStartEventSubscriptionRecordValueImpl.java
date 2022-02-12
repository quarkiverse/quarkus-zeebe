package io.quarkiverse.zeebe.test.records;

import java.util.Map;

import io.camunda.zeebe.protocol.record.value.MessageStartEventSubscriptionRecordValue;
import io.quarkiverse.zeebe.test.ProtoBufUtil;
import io.zeebe.exporter.proto.Schema;

public class MessageStartEventSubscriptionRecordValueImpl extends RecordValueImpl
        implements MessageStartEventSubscriptionRecordValue {

    private final Schema.MessageStartEventSubscriptionRecord record;

    private Map<String, Object> variables;

    public MessageStartEventSubscriptionRecordValueImpl(Schema.MessageStartEventSubscriptionRecord record) {
        this.record = record;
    }

    @Override
    public long getProcessDefinitionKey() {
        return record.getProcessDefinitionKey();
    }

    @Override
    public String getBpmnProcessId() {
        return record.getBpmnProcessId();
    }

    @Override
    public String getStartEventId() {
        return record.getStartEventId();
    }

    @Override
    public String getMessageName() {
        return record.getMessageName();
    }

    @Override
    public long getProcessInstanceKey() {
        return record.getProcessInstanceKey();
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
