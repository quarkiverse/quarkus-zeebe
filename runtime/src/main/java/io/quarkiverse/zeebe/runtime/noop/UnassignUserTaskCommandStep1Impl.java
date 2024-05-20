package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.command.UnassignUserTaskCommandStep1;
import io.camunda.zeebe.client.api.response.UnassignUserTaskResponse;

public class UnassignUserTaskCommandStep1Impl extends AbstractStep<UnassignUserTaskResponse>
        implements UnassignUserTaskCommandStep1 {

    @Override
    protected UnassignUserTaskResponse create() {
        return new UnassignUserTaskResponse() {
        };
    }
}
