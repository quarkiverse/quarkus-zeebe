package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.command.AssignUserTaskCommandStep1;
import io.camunda.zeebe.client.api.response.AssignUserTaskResponse;

public class AssignUserTaskCommandStep1Impl extends AbstractStep<AssignUserTaskResponse> implements AssignUserTaskCommandStep1 {

    @Override
    protected AssignUserTaskResponse create() {
        return new AssignUserTaskResponse() {
        };
    }

    @Override
    public AssignUserTaskCommandStep1 action(String action) {
        return this;
    }

    @Override
    public AssignUserTaskCommandStep1 assignee(String assignee) {
        return this;
    }

    @Override
    public AssignUserTaskCommandStep1 allowOverride(boolean allowOverride) {
        return this;
    }
}
