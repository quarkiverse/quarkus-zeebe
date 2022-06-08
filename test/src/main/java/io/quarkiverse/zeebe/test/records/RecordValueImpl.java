package io.quarkiverse.zeebe.test.records;

import io.camunda.zeebe.protocol.record.RecordValue;
import io.zeebe.exporter.proto.Schema;

public abstract class RecordValueImpl implements RecordValue {

    abstract Schema.RecordMetadata getMetadata();

}
