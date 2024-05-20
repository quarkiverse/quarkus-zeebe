package io.quarkiverse.zeebe.runtime.noop;

import java.time.Duration;
import java.util.List;

import io.camunda.zeebe.client.api.worker.*;

public class JobWorkerBuilderStep1Impl implements JobWorkerBuilderStep1, JobWorkerBuilderStep1.JobWorkerBuilderStep2,
        JobWorkerBuilderStep1.JobWorkerBuilderStep3 {
    @Override
    public JobWorkerBuilderStep2 jobType(String type) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 handler(JobHandler handler) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 timeout(long timeout) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 timeout(Duration timeout) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 name(String workerName) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 maxJobsActive(int maxJobsActive) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 pollInterval(Duration pollInterval) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 requestTimeout(Duration requestTimeout) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 fetchVariables(List<String> fetchVariables) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 fetchVariables(String... fetchVariables) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 backoffSupplier(BackoffSupplier backoffSupplier) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 streamEnabled(boolean isStreamEnabled) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 streamTimeout(Duration timeout) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 metrics(JobWorkerMetrics metrics) {
        return this;
    }

    @Override
    public JobWorker open() {
        return new JobWorker() {
            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public boolean isClosed() {
                return true;
            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public JobWorkerBuilderStep3 tenantId(String tenantId) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 tenantIds(List<String> tenantIds) {
        return this;
    }

    @Override
    public JobWorkerBuilderStep3 tenantIds(String... tenantIds) {
        return this;
    }

}
