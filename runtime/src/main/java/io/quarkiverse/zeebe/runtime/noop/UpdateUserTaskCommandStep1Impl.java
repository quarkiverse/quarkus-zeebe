package io.quarkiverse.zeebe.runtime.noop;

import java.util.List;

import io.camunda.zeebe.client.api.command.UpdateUserTaskCommandStep1;
import io.camunda.zeebe.client.api.response.UpdateUserTaskResponse;

public class UpdateUserTaskCommandStep1Impl extends AbstractStep<UpdateUserTaskResponse> implements UpdateUserTaskCommandStep1 {
    @Override
    public UpdateUserTaskCommandStep1 action(String action) {
        return this;
    }

    @Override
    public UpdateUserTaskCommandStep1 dueDate(String dueDate) {
        return this;
    }

    @Override
    public UpdateUserTaskCommandStep1 clearDueDate() {
        return this;
    }

    @Override
    public UpdateUserTaskCommandStep1 followUpDate(String followUpDate) {
        return this;
    }

    @Override
    public UpdateUserTaskCommandStep1 clearFollowUpDate() {
        return this;
    }

    @Override
    public UpdateUserTaskCommandStep1 candidateGroups(List<String> candidateGroups) {
        return this;
    }

    @Override
    public UpdateUserTaskCommandStep1 candidateGroups(String... candidateGroups) {
        return this;
    }

    @Override
    public UpdateUserTaskCommandStep1 clearCandidateGroups() {
        return this;
    }

    @Override
    public UpdateUserTaskCommandStep1 candidateUsers(List<String> candidateUsers) {
        return this;
    }

    @Override
    public UpdateUserTaskCommandStep1 candidateUsers(String... candidateUsers) {
        return this;
    }

    @Override
    public UpdateUserTaskCommandStep1 clearCandidateUsers() {
        return this;
    }

    @Override
    protected UpdateUserTaskResponse create() {
        return new UpdateUserTaskResponse() {
        };
    }
}
