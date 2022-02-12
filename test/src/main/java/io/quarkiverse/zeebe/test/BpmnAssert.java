package io.quarkiverse.zeebe.test;

import io.camunda.zeebe.client.api.response.*;
import io.camunda.zeebe.process.test.assertions.DeploymentAssert;
import io.camunda.zeebe.process.test.assertions.JobAssert;
import io.camunda.zeebe.process.test.assertions.MessageAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.process.test.testengine.RecordStreamSource;

public class BpmnAssert {

    static RecordStreamSource recordStreamSource;

    public static void init(final RecordStreamSource recordStreamSource) {
        BpmnAssert.recordStreamSource = recordStreamSource;
    }

    public static RecordStreamSource getRecordStreamSource() {
        if (recordStreamSource == null) {
            throw new AssertionError(
                    "No RecordStreamSource is set. Please make sure you are using the @ZeebeTestResource annotation.");
        }
        return recordStreamSource;
    }

    public static ProcessInstanceAssert assertThat(final ProcessInstanceEvent instanceEvent) {
        return new ProcessInstanceAssert(
                instanceEvent.getProcessInstanceKey(), getRecordStreamSource());
    }

    public static ProcessInstanceAssert assertThat(final ProcessInstanceResult instanceResult) {
        return new ProcessInstanceAssert(
                instanceResult.getProcessInstanceKey(), getRecordStreamSource());
    }

    public static ProcessInstanceAssert assertThat(
            final InspectedProcessInstance inspectedProcessInstance) {
        return new ProcessInstanceAssert(
                inspectedProcessInstance.getProcessInstanceKey(), getRecordStreamSource());
    }

    public static JobAssert assertThat(final ActivatedJob activatedJob) {
        return new JobAssert(activatedJob, getRecordStreamSource());
    }

    public static DeploymentAssert assertThat(final DeploymentEvent deploymentEvent) {
        return new DeploymentAssert(deploymentEvent, getRecordStreamSource());
    }

    public static MessageAssert assertThat(final PublishMessageResponse publishMessageResponse) {
        return new MessageAssert2(publishMessageResponse, getRecordStreamSource());
    }

    public static class MessageAssert2 extends MessageAssert {
        protected MessageAssert2(final PublishMessageResponse actual, final RecordStreamSource recordStreamSource) {
            super(actual, recordStreamSource);
        }
    }
}
