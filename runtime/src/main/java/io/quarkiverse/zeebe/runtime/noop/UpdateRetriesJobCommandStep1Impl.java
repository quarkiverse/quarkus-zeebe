package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.command.UpdateRetriesJobCommandStep1;
import io.camunda.zeebe.client.api.response.UpdateRetriesJobResponse;

public class UpdateRetriesJobCommandStep1Impl extends AbstractStep<UpdateRetriesJobResponse>
        implements UpdateRetriesJobCommandStep1, UpdateRetriesJobCommandStep1.UpdateRetriesJobCommandStep2 {

    @Override
    public UpdateRetriesJobCommandStep2 retries(int retries) {
        return this;
    }

    @Override
    protected UpdateRetriesJobResponse create() {
        return new UpdateRetriesJobResponse() {
        };
    }
}
