package io.quarkiverse.zeebe.runtime.noop;

import java.time.Duration;

import io.camunda.zeebe.client.api.command.UpdateJobCommandStep1;
import io.camunda.zeebe.client.api.response.UpdateJobResponse;
import io.camunda.zeebe.client.protocol.rest.JobChangeset;

public class UpdateJobCommandStep1Impl extends AbstractStep<UpdateJobResponse>
        implements UpdateJobCommandStep1, UpdateJobCommandStep1.UpdateJobCommandStep2 {
    @Override
    public UpdateJobCommandStep2 update(JobChangeset jobChangeset) {
        return this;
    }

    @Override
    public UpdateJobCommandStep2 update(Integer retries, Long timeout) {
        return this;
    }

    @Override
    public UpdateJobCommandStep2 updateRetries(int retries) {
        return this;
    }

    @Override
    public UpdateJobCommandStep2 updateTimeout(long timeout) {
        return this;
    }

    @Override
    public UpdateJobCommandStep2 updateTimeout(Duration timeout) {
        return this;
    }
}
