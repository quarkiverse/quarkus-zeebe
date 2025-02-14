package io.quarkiverse.zeebe.runtime.noop;

import java.time.Duration;

import io.camunda.zeebe.client.api.command.UpdateTimeoutJobCommandStep1;
import io.camunda.zeebe.client.api.response.UpdateTimeoutJobResponse;

public class UpdateTimeoutJobCommandStep1Impl extends AbstractStep<UpdateTimeoutJobResponse>
        implements UpdateTimeoutJobCommandStep1, UpdateTimeoutJobCommandStep1.UpdateTimeoutJobCommandStep2 {

    @Override
    public UpdateTimeoutJobCommandStep2 timeout(long timeout) {
        return this;
    }

    @Override
    public UpdateTimeoutJobCommandStep2 timeout(Duration timeout) {
        return this;
    }

    @Override
    protected UpdateTimeoutJobResponse create() {
        return new UpdateTimeoutJobResponse() {
        };
    }

    @Override
    public UpdateTimeoutJobCommandStep1 useRest() {
        return this;
    }

    @Override
    public UpdateTimeoutJobCommandStep1 useGrpc() {
        return this;
    }

    @Override
    public UpdateTimeoutJobCommandStep2 operationReference(long operationReference) {
        return this;
    }
}
