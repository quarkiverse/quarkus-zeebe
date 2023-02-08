package io.quarkiverse.zeebe;

import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.command.FinalCommandStep;
import io.camunda.zeebe.client.api.command.ThrowErrorCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.BackoffSupplier;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkiverse.zeebe.runtime.metrics.MetricsRecorder;

public class JobWorkerCommand {

    private final FinalCommandStep<?> command;

    private JobWorkerExceptionHandler exceptionHandler;

    private BackoffSupplier backoffSupplier;

    private String metricsActionName;

    private String metricsFailedActionName;

    private ActivatedJob job;

    private int counter = 0;

    private long retryDelay = 50l;

    private int maxRetries = 20;

    private MetricsRecorder metricsRecorder;

    public JobWorkerCommand(FinalCommandStep<?> command, ActivatedJob job, String metricsActionName,
            String metricsFailedActionName) {
        this.command = command;
        this.job = job;
        this.metricsActionName = metricsActionName;
        this.metricsFailedActionName = metricsFailedActionName;
    }

    public JobWorkerCommand backoffSupplier(BackoffSupplier backoffSupplier) {
        this.backoffSupplier = backoffSupplier;
        return this;
    }

    public JobWorkerCommand metricsRecorder(MetricsRecorder metricsRecorder) {
        this.metricsRecorder = metricsRecorder;
        return this;
    }

    public JobWorkerCommand maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public JobWorkerCommand retryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }

    public JobWorkerCommand exceptionHandler(JobWorkerExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public void send() {
        counter++;
        command.send().handle((o, ex) -> {
            if (ex != null) {
                metricsRecorder.increase(MetricsRecorder.METRIC_NAME_JOB, metricsFailedActionName, job.getType());
                exceptionHandler.handleError(this, ex);
                return null;
            }
            metricsRecorder.increase(MetricsRecorder.METRIC_NAME_JOB, metricsActionName, job.getType());
            return null;
        });
    }

    public static JobWorkerCommand createJobWorkerCommand(JobClient client, ActivatedJob job, Object result) {
        CompleteJobCommandStep1 tmp = createCompleteCommand(client, job, result);
        return new JobWorkerCommand(tmp, job, MetricsRecorder.ACTION_COMPLETED, MetricsRecorder.ACTION_COMPLETED_FAILED);
    }

    public static JobWorkerCommand createJobWorkerCommand(JobClient client, ActivatedJob job, ZeebeBpmnError bpmnError) {
        ThrowErrorCommandStep1.ThrowErrorCommandStep2 tmp = client.newThrowErrorCommand(job.getKey())
                .errorCode(bpmnError.getErrorCode())
                .errorMessage(bpmnError.getErrorMessage());
        return new JobWorkerCommand(tmp, job, MetricsRecorder.ACTION_BPMN_ERROR, MetricsRecorder.ACTION_BPMN_ERROR_FAILED);
    }

    private static CompleteJobCommandStep1 createCompleteCommand(JobClient client, ActivatedJob job, Object result) {
        CompleteJobCommandStep1 completeCommand = client.newCompleteCommand(job.getKey());
        if (result == null) {
            return completeCommand;
        }
        if (result.getClass().isAssignableFrom(Map.class)) {
            return completeCommand.variables((Map) result);
        }
        if (result.getClass().isAssignableFrom(String.class)) {
            return completeCommand.variables((String) result);
        }
        if (result.getClass().isAssignableFrom(InputStream.class)) {
            return completeCommand.variables((InputStream) result);
        }
        return completeCommand.variables(result);
    }

    public void retry(ScheduledExecutorService scheduledExecutorService) {
        scheduledExecutorService.schedule(this::send, retryDelay, TimeUnit.MILLISECONDS);
    }

    public void supplyRetryDelay() {
        retryDelay = backoffSupplier.supplyRetryDelay(retryDelay);
    }

    public boolean canRetry() {
        if (Instant.now().getEpochSecond() > job.getDeadline()) {
            return false;
        }
        if (counter >= maxRetries) {
            return false;
        }
        return true;
    }
}
