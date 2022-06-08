package io.quarkiverse.zeebe.test;

import io.camunda.zeebe.client.api.response.*;
import io.camunda.zeebe.process.test.assertions.DeploymentAssert;
import io.camunda.zeebe.process.test.assertions.JobAssert;
import io.camunda.zeebe.process.test.assertions.MessageAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;

public class BpmnAssert {

    static RecordStream recordStreamSource;

    public static void init(final RecordStream recordStreamSource) {
        BpmnAssert.recordStreamSource = recordStreamSource;
    }

    public static RecordStream getRecordStream() {
        if (recordStreamSource == null) {
            throw new AssertionError(
                    "No RecordStream is set. Please make sure you are using the @ZeebeTestResource annotation.");
        }
        return recordStreamSource;
    }

    public static ProcessInstanceAssert assertThat(final ProcessInstanceEvent instanceEvent) {
        return new ProcessInstanceAssert(
                instanceEvent.getProcessInstanceKey(), getRecordStream());
    }

    public static ProcessInstanceAssert assertThat(final ProcessInstanceResult instanceResult) {
        return new ProcessInstanceAssert(
                instanceResult.getProcessInstanceKey(), getRecordStream());
    }

    public static ProcessInstanceAssert assertThat(
            final InspectedProcessInstance inspectedProcessInstance) {
        return new ProcessInstanceAssert(
                inspectedProcessInstance.getProcessInstanceKey(), getRecordStream());
    }

    public static JobAssert assertThat(final ActivatedJob activatedJob) {
        return new JobAssert(activatedJob, getRecordStream());
    }

    public static DeploymentAssert assertThat(final DeploymentEvent deploymentEvent) {
        return new DeploymentAssert(deploymentEvent, getRecordStream());
    }

    public static MessageAssert assertThat(final PublishMessageResponse publishMessageResponse) {
        return new MessageAssert2(publishMessageResponse, getRecordStream());
    }

    private static class MessageAssert2 extends MessageAssert {
        protected MessageAssert2(final PublishMessageResponse actual, final RecordStream recordStreamSource) {
            super(actual, recordStreamSource);
        }
    }
}
