package io.quarkiverse.zeebe.runtime.noop;

import java.util.Map;

import io.camunda.zeebe.client.api.command.CompleteUserTaskCommandStep1;
import io.camunda.zeebe.client.api.response.CompleteUserTaskResponse;

public class CompleteUserTaskCommandStep1Impl extends AbstractStep<CompleteUserTaskResponse>
        implements CompleteUserTaskCommandStep1 {

    @Override
    public CompleteUserTaskCommandStep1 action(String action) {
        return this;
    }

    @Override
    public CompleteUserTaskCommandStep1 variables(Map<String, Object> variables) {
        return this;
    }

    @Override
    protected CompleteUserTaskResponse create() {
        return new CompleteUserTaskResponse() {
        };
    }
}
