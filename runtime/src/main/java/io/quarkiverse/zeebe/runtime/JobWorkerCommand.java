package io.quarkiverse.zeebe.runtime;

import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;

import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.command.FinalCommandStep;
import io.camunda.zeebe.client.api.command.ThrowErrorCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.BackoffSupplier;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkiverse.zeebe.JobWorkerExceptionHandler;
import io.quarkiverse.zeebe.ZeebeBpmnError;
import io.quarkiverse.zeebe.runtime.metrics.MetricsRecorder;
import io.quarkiverse.zeebe.runtime.tracing.TracingRecorder;
import io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing;

public class JobWorkerCommand {

    private static final Logger LOG = Logger.getLogger(JobWorkerCommand.class);

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

    private TracingRecorder.TracingContext tracingContext;

    public JobWorkerCommand(FinalCommandStep<?> command, ActivatedJob job, String metricsActionName,
            String metricsFailedActionName) {
        this.command = command;
        this.job = job;
        this.metricsActionName = metricsActionName;
        this.metricsFailedActionName = metricsFailedActionName;
    }

    public void send() {
        counter++;
        command.send().handle((o, ex) -> {
            if (ex != null) {
                // tracing command failed
                tracingContext.error(ZeebeTracing.JOB_CMD_EXCEPTION, ex);

                // metrics
                metricsRecorder.increase(MetricsRecorder.METRIC_NAME_JOB, metricsFailedActionName, job.getType());

                // handle error (retry)
                try {
                    exceptionHandler.handleError(this, ex);
                } catch (Throwable t) {
                    if (t instanceof JobWorkerExceptionHandler.WarningException) {
                        LOG.warn(t.getMessage());
                    } else {
                        LOG.error(t.getMessage());
                    }
                    // tracing error handler
                    tracingContext.error(ZeebeTracing.JOB_ERROR_HANDLER_EXCEPTION, t);
                    tracingContext.close();
                }
                return null;
            }

            // metrics
            metricsRecorder.increase(MetricsRecorder.METRIC_NAME_JOB, metricsActionName, job.getType());

            // tracing
            tracingContext.ok();
            tracingContext.close();
            return null;
        });
    }

    public static JobWorkerCommand createJobWorkerCommand(JobClient client, ActivatedJob job, Object result) {
        CompleteJobCommandStep1 tmp = createCompleteCommand(client, job, result);
        return new JobWorkerCommand(tmp, job, MetricsRecorder.ACTION_COMPLETED, MetricsRecorder.ACTION_COMPLETED_FAILED);
    }

    public static JobWorkerCommand createThrowErrorCommand(JobClient client, ActivatedJob job, ZeebeBpmnError bpmnError) {
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
        if (Map.class.isAssignableFrom(result.getClass())) {
            @SuppressWarnings("unchecked")
            Map<String, Object> var = (Map<String, Object>) result;
            return completeCommand.variables(var);
        }
        if (String.class.isAssignableFrom(result.getClass())) {
            return completeCommand.variables((String) result);
        }
        if (InputStream.class.isAssignableFrom(result.getClass())) {
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
        long now = Instant.now().getEpochSecond();
        if (now > job.getDeadline()) {
            LOG.warnf("Command %s type %s cannot be repeated: deadline time [now: %s, deadline: %s]",
                    command.getClass().getName(), job.getType(), now, job.getDeadline());
            return false;
        }
        if (counter >= maxRetries) {
            LOG.warnf("Command %s type %s cannot be repeated: no retries are left [counter: %s, max-retries: %s]",
                    command.getClass().getName(), job.getType(), counter, maxRetries);
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "JobWorkerCommand{" +
                "command=" + command.getClass().getName() +
                ", counter=" + counter +
                ", maxRetries=" + maxRetries +
                '}';
    }

    public JobWorkerCommand request(TracingRecorder.TracingContext tracingContext, MetricsRecorder metricsRecorder,
            BackoffSupplier backoffSupplier, JobWorkerExceptionHandler exceptionHandler, int maxRetries, long retryDelay) {
        this.tracingContext = tracingContext;
        this.metricsRecorder = metricsRecorder;
        this.backoffSupplier = backoffSupplier;
        this.exceptionHandler = exceptionHandler;
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
        return this;
    }
}
