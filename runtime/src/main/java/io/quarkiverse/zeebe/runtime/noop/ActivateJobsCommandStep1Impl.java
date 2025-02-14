package io.quarkiverse.zeebe.runtime.noop;

import java.time.Duration;
import java.util.List;

import io.camunda.zeebe.client.api.command.ActivateJobsCommandStep1;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;

public class ActivateJobsCommandStep1Impl extends AbstractStep<ActivateJobsResponse>
        implements ActivateJobsCommandStep1.ActivateJobsCommandStep3, ActivateJobsCommandStep1,
        ActivateJobsCommandStep1.ActivateJobsCommandStep2 {

    @Override
    public ActivateJobsCommandStep2 jobType(String jobType) {
        return this;
    }

    @Override
    public ActivateJobsCommandStep3 maxJobsToActivate(int maxJobsToActivate) {
        return this;
    }

    @Override
    public ActivateJobsCommandStep3 timeout(Duration timeout) {
        return this;
    }

    @Override
    public ActivateJobsCommandStep3 workerName(String workerName) {
        return this;
    }

    @Override
    public ActivateJobsCommandStep3 fetchVariables(List<String> fetchVariables) {
        return this;
    }

    @Override
    public ActivateJobsCommandStep3 fetchVariables(String... fetchVariables) {
        return this;
    }

    @Override
    public ActivateJobsCommandStep3 tenantId(String tenantId) {
        return this;
    }

    @Override
    public ActivateJobsCommandStep3 tenantIds(List<String> tenantIds) {
        return this;
    }

    @Override
    public ActivateJobsCommandStep3 tenantIds(String... tenantIds) {
        return this;
    }

    @Override
    protected ActivateJobsResponse create() {
        return List::of;
    }

    @Override
    public ActivateJobsCommandStep1 useRest() {
        return this;
    }

    @Override
    public ActivateJobsCommandStep1 useGrpc() {
        return this;
    }
}
