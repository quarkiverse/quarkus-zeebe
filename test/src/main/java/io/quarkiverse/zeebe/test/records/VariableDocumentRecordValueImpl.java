package io.quarkiverse.zeebe.test.records;

import java.util.Map;

import io.camunda.zeebe.protocol.record.value.VariableDocumentRecordValue;
import io.camunda.zeebe.protocol.record.value.VariableDocumentUpdateSemantic;
import io.quarkiverse.zeebe.test.ProtoBufUtil;
import io.zeebe.exporter.proto.Schema;

public class VariableDocumentRecordValueImpl extends RecordValueImpl implements VariableDocumentRecordValue {

    private final Schema.VariableDocumentRecord record;

    private Map<String, Object> variables;

    public VariableDocumentRecordValueImpl(Schema.VariableDocumentRecord record) {
        this.record = record;
    }

    @Override
    public long getScopeKey() {
        return record.getScopeKey();
    }

    @Override
    public VariableDocumentUpdateSemantic getUpdateSemantics() {
        if (Schema.VariableDocumentRecord.UpdateSemantics.UNKNOWN_UPDATE_SEMANTICS.equals(record.getUpdateSemantics())
                || Schema.VariableDocumentRecord.UpdateSemantics.UNRECOGNIZED.equals(record.getUpdateSemantics())) {
            return null;
        }
        return VariableDocumentUpdateSemantic.valueOf(record.getUpdateSemantics().name());
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
