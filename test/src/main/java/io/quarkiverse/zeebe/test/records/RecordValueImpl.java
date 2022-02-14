package io.quarkiverse.zeebe.test.records;

import io.camunda.zeebe.protocol.impl.encoding.MsgPackConverter;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.zeebe.exporter.proto.Schema;

public abstract class RecordValueImpl implements RecordValue {

    abstract Schema.RecordMetadata getMetadata();

    @Override
    public String toJson() {
        return MsgPackConverter.convertJsonSerializableObjectToJson(this);
    }
}
