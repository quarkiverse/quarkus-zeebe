package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.command.DeleteResourceCommandStep1;
import io.camunda.zeebe.client.api.response.DeleteResourceResponse;

public class DeleteResourceCommandStep1Impl extends AbstractStep<DeleteResourceResponse> implements DeleteResourceCommandStep1 {

    @Override
    protected DeleteResourceResponse create() {
        return new DeleteResourceResponse() {
        };
    }

    @Override
    public DeleteResourceCommandStep1 useRest() {
        return this;
    }

    @Override
    public DeleteResourceCommandStep1 useGrpc() {
        return this;
    }

    @Override
    public DeleteResourceCommandStep1 operationReference(long operationReference) {
        return this;
    }
}
