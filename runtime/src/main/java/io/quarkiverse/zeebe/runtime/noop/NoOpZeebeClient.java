package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientConfiguration;
import io.camunda.zeebe.client.api.command.*;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1;
import io.camunda.zeebe.client.impl.ZeebeClientBuilderImpl;

public class NoOpZeebeClient implements ZeebeClient {

    @Override
    public TopologyRequestStep1 newTopologyRequest() {
        return new TopologyRequestStep1Impl();
    }

    @Override
    public ZeebeClientConfiguration getConfiguration() {
        return new ZeebeClientBuilderImpl();
    }

    @Override
    public void close() {

    }

    @Override
    @Deprecated
    public DeployProcessCommandStep1 newDeployCommand() {
        return null;
    }

    @Override
    public DeployResourceCommandStep1 newDeployResourceCommand() {
        return new DeployResourceCommandStep1Impl();
    }

    @Override
    public CreateProcessInstanceCommandStep1 newCreateInstanceCommand() {
        return new CreateProcessInstanceCommandStep1Impl();
    }

    @Override
    public ModifyProcessInstanceCommandStep1 newModifyProcessInstanceCommand(long processInstanceKey) {
        return new ModifyProcessInstanceCommandStep1Impl();
    }

    @Override
    public MigrateProcessInstanceCommandStep1 newMigrateProcessInstanceCommand(long processInstanceKey) {
        return new MigrateProcessInstanceCommandStep1Impl();
    }

    @Override
    public CancelProcessInstanceCommandStep1 newCancelInstanceCommand(long processInstanceKey) {
        return new CancelProcessInstanceCommandStep1Impl();
    }

    @Override
    public SetVariablesCommandStep1 newSetVariablesCommand(long elementInstanceKey) {
        return new SetVariablesCommandStep1Impl();
    }

    @Override
    public EvaluateDecisionCommandStep1 newEvaluateDecisionCommand() {
        return new EvaluateDecisionCommandStep1Impl();
    }

    @Override
    public PublishMessageCommandStep1 newPublishMessageCommand() {
        return new PublishMessageCommandStep1Impl();
    }

    @Override
    public BroadcastSignalCommandStep1 newBroadcastSignalCommand() {
        return new BroadcastSignalCommandStep1Impl();
    }

    @Override
    public ResolveIncidentCommandStep1 newResolveIncidentCommand(long incidentKey) {
        return new ResolveIncidentCommandStep1Impl();
    }

    @Override
    public UpdateRetriesJobCommandStep1 newUpdateRetriesCommand(long jobKey) {
        return new UpdateRetriesJobCommandStep1Impl();
    }

    @Override
    public UpdateRetriesJobCommandStep1 newUpdateRetriesCommand(ActivatedJob job) {
        return new UpdateRetriesJobCommandStep1Impl();
    }

    @Override
    public UpdateTimeoutJobCommandStep1 newUpdateTimeoutCommand(long jobKey) {
        return new UpdateTimeoutJobCommandStep1Impl();
    }

    @Override
    public UpdateTimeoutJobCommandStep1 newUpdateTimeoutCommand(ActivatedJob job) {
        return new UpdateTimeoutJobCommandStep1Impl();
    }

    @Override
    public JobWorkerBuilderStep1 newWorker() {
        return new JobWorkerBuilderStep1Impl();
    }

    @Override
    public DeleteResourceCommandStep1 newDeleteResourceCommand(long resourceKey) {
        return new DeleteResourceCommandStep1Impl();
    }

    @Override
    public CompleteUserTaskCommandStep1 newUserTaskCompleteCommand(long userTaskKey) {
        return new CompleteUserTaskCommandStep1Impl();
    }

    @Override
    public AssignUserTaskCommandStep1 newUserTaskAssignCommand(long userTaskKey) {
        return new AssignUserTaskCommandStep1Impl();
    }

    @Override
    public UpdateUserTaskCommandStep1 newUserTaskUpdateCommand(long userTaskKey) {
        return new UpdateUserTaskCommandStep1Impl();
    }

    @Override
    public UnassignUserTaskCommandStep1 newUserTaskUnassignCommand(long userTaskKey) {
        return new UnassignUserTaskCommandStep1Impl();
    }

    @Override
    public CompleteJobCommandStep1 newCompleteCommand(long jobKey) {
        return new CompleteJobCommandStep1Impl();
    }

    @Override
    public CompleteJobCommandStep1 newCompleteCommand(ActivatedJob job) {
        return new CompleteJobCommandStep1Impl();
    }

    @Override
    public FailJobCommandStep1 newFailCommand(long jobKey) {
        return new FailJobCommandStep1Impl();
    }

    @Override
    public FailJobCommandStep1 newFailCommand(ActivatedJob job) {
        return new FailJobCommandStep1Impl();
    }

    @Override
    public ThrowErrorCommandStep1 newThrowErrorCommand(long jobKey) {
        return new ThrowErrorCommandStep1Impl();
    }

    @Override
    public ThrowErrorCommandStep1 newThrowErrorCommand(ActivatedJob job) {
        return new ThrowErrorCommandStep1Impl();
    }

    @Override
    public ActivateJobsCommandStep1 newActivateJobsCommand() {
        return new ActivateJobsCommandStep1Impl();
    }

    @Override
    public StreamJobsCommandStep1 newStreamJobsCommand() {
        return new StreamJobsCommandStep1Impl();
    }

}
