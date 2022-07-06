package io.quarkiverse.zeebe.test;

import java.util.Optional;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.filters.StreamFilter;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;

public class BpmnUtil {

    /**
     * Find child process instance.
     *
     * @param instanceEvent parent process instance
     * @param childProcessId child process id
     * @return the child process instance event or {@code null}
     */
    public static ProcessInstanceEvent findChildProcess(final ProcessInstanceEvent instanceEvent, final String childProcessId) {

        Optional<Record<ProcessInstanceRecordValue>> tmp = StreamFilter.processInstance(BpmnAssert.recordStreamSource)
                .withParentProcessInstanceKey(instanceEvent.getProcessInstanceKey())
                .withRejectionType(RejectionType.NULL_VAL)
                .withRecordType(RecordType.COMMAND)
                .withIntent(ProcessInstanceIntent.ACTIVATE_ELEMENT)
                .withBpmnElementType(BpmnElementType.PROCESS)
                .withBpmnProcessId(childProcessId)
                .stream()
                .findFirst();

        if (tmp.isPresent()) {
            ProcessInstanceRecordValue value = tmp.get().getValue();
            return new ProcessInstanceEvent() {

                @Override
                public long getProcessDefinitionKey() {
                    return value.getProcessDefinitionKey();
                }

                @Override
                public String getBpmnProcessId() {
                    return value.getBpmnProcessId();
                }

                @Override
                public int getVersion() {
                    return value.getVersion();
                }

                @Override
                public long getProcessInstanceKey() {
                    return value.getProcessInstanceKey();
                }
            };
        }
        return null;
    }

}
