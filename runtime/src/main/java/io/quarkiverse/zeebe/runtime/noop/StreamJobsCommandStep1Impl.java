package io.quarkiverse.zeebe.runtime.noop;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import io.camunda.zeebe.client.api.command.StreamJobsCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.StreamJobsResponse;
import io.camunda.zeebe.client.impl.response.StreamJobsResponseImpl;

public class StreamJobsCommandStep1Impl extends AbstractStep<StreamJobsResponse> implements
        StreamJobsCommandStep1.StreamJobsCommandStep3, StreamJobsCommandStep1, StreamJobsCommandStep1.StreamJobsCommandStep2 {

    @Override
    public StreamJobsCommandStep2 jobType(String jobType) {
        return this;
    }

    @Override
    public StreamJobsCommandStep3 consumer(Consumer<ActivatedJob> consumer) {
        return this;
    }

    @Override
    public StreamJobsCommandStep3 timeout(Duration timeout) {
        return this;
    }

    @Override
    public StreamJobsCommandStep3 workerName(String workerName) {
        return this;
    }

    @Override
    public StreamJobsCommandStep3 fetchVariables(List<String> fetchVariables) {
        return this;
    }

    @Override
    public StreamJobsCommandStep3 fetchVariables(String... fetchVariables) {
        return this;
    }

    @Override
    public StreamJobsCommandStep3 tenantId(String tenantId) {
        return this;
    }

    @Override
    public StreamJobsCommandStep3 tenantIds(List<String> tenantIds) {
        return this;
    }

    @Override
    public StreamJobsCommandStep3 tenantIds(String... tenantIds) {
        return this;
    }

    @Override
    protected StreamJobsResponse create() {
        return new StreamJobsResponseImpl();
    }

}
