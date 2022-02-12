package io.quarkiverse.zeebe.test.records;

import java.util.Map;

import io.camunda.zeebe.protocol.record.value.MessageRecordValue;
import io.quarkiverse.zeebe.test.ProtoBufUtil;
import io.zeebe.exporter.proto.Schema;

public class MessageRecordValueImpl extends RecordValueImpl implements MessageRecordValue {

    private final Schema.MessageRecord record;

    private Map<String, Object> variables;

    public MessageRecordValueImpl(Schema.MessageRecord record) {
        this.record = record;
    }

    @Override
    public String getName() {
        return record.getName();
    }

    @Override
    public String getCorrelationKey() {
        return record.getCorrelationKey();
    }

    @Override
    public String getMessageId() {
        return record.getMessageId();
    }

    @Override
    public long getTimeToLive() {
        return record.getTimeToLive();
    }

    @Override
    public long getDeadline() {
        return 0;
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
