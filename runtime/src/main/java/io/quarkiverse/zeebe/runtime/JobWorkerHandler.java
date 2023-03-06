package io.quarkiverse.zeebe.runtime;

import org.jboss.logging.Logger;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.BackoffSupplier;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.impl.worker.ExponentialBackoffBuilderImpl;
import io.quarkiverse.zeebe.JobWorkerExceptionHandler;
import io.quarkiverse.zeebe.ZeebeBpmnError;
import io.quarkiverse.zeebe.runtime.metrics.MetricsRecorder;
import io.quarkiverse.zeebe.runtime.tracing.TracingRecorder;
import io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing;

/**
 * Invokes a job worker business method of a bean.
 */
public class JobWorkerHandler implements JobHandler {

    private static final Logger LOG = Logger.getLogger(JobWorkerHandler.class);

    private JobWorkerMetadata jobWorkerMetadata;

    private JobWorkerInvoker invoker;

    private JobWorkerExceptionHandler exceptionHandler;

    private BackoffSupplier backoffSupplier;

    private ZeebeRuntimeConfig.AutoCompleteConfig autoCompleteConfig;

    private MetricsRecorder metricsRecorder;

    private TracingRecorder tracingRecorder;

    private String spanName;

    public JobWorkerHandler(JobWorkerMetadata jobWorkerMetadata, JobWorkerInvoker invoker, MetricsRecorder metricsRecorder,
            JobWorkerExceptionHandler exceptionHandler, ZeebeRuntimeConfig.AutoCompleteConfig autoCompleteConfig,
            TracingRecorder tracingRecorder) {
        this.jobWorkerMetadata = jobWorkerMetadata;
        this.invoker = invoker;
        this.metricsRecorder = metricsRecorder;
        this.exceptionHandler = exceptionHandler;
        this.autoCompleteConfig = autoCompleteConfig;
        this.tracingRecorder = tracingRecorder;
        this.spanName = jobWorkerMetadata.workerValue.name;
        if (spanName == null || spanName.isEmpty()) {
            spanName = jobWorkerMetadata.methodName;
        }

        if (jobWorkerMetadata.workerValue.autoComplete) {
            this.backoffSupplier = new ExponentialBackoffBuilderImpl()
                    .maxDelay(autoCompleteConfig.expMaxDelay)
                    .minDelay(autoCompleteConfig.expMinDelay)
                    .backoffFactor(autoCompleteConfig.expBackoffFactor)
                    .jitterFactor(autoCompleteConfig.expJitterFactor)
                    .build();
        }
    }

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        LOG.tracef("Handle %s and invoke worker %s", job, jobWorkerMetadata.workerValue);

        TracingRecorder.TracingContext tracingContext = tracingRecorder
                .createTracingContext(jobWorkerMetadata.declaringClassName, jobWorkerMetadata.methodName, spanName, job);
        try {
            metricsRecorder.increase(MetricsRecorder.METRIC_NAME_JOB, MetricsRecorder.ACTION_ACTIVATED, job.getType());
            Object result;
            try {
                result = invoker.invoke(client, job).toCompletableFuture().get();
            } catch (Throwable throwable) {
                metricsRecorder.increase(MetricsRecorder.METRIC_NAME_JOB, MetricsRecorder.ACTION_FAILED, job.getType());
                throw throwable;
            }

            if (jobWorkerMetadata.workerValue.autoComplete) {
                JobWorkerCommand.createJobWorkerCommand(client, job, result)
                        .tracingContext(tracingContext)
                        .metricsRecorder(metricsRecorder)
                        .backoffSupplier(backoffSupplier)
                        .exceptionHandler(exceptionHandler)
                        .maxRetries(autoCompleteConfig.maxRetries)
                        .retryDelay(autoCompleteConfig.retryDelay)
                        .send();
            }
        } catch (ZeebeBpmnError error) {
            tracingContext.error(ZeebeTracing.JOB_EXCEPTION, error);
            LOG.tracef("Caught JobWorker BPMN error on %s", job);
            JobWorkerCommand.createJobWorkerCommand(client, job, error)
                    .tracingContext(tracingContext)
                    .metricsRecorder(metricsRecorder)
                    .backoffSupplier(backoffSupplier)
                    .exceptionHandler(exceptionHandler)
                    .maxRetries(autoCompleteConfig.maxRetries)
                    .retryDelay(autoCompleteConfig.retryDelay)
                    .send();
        } catch (Exception exception) {
            tracingContext.error(ZeebeTracing.JOB_EXCEPTION, exception);
            LOG.tracef("Caught exception %s error on %s", exception.getMessage(), job);
            if (jobWorkerMetadata.workerValue.autoComplete) {
                tracingContext.close();
            }
            throw exception;
        } finally {
            if (!jobWorkerMetadata.workerValue.autoComplete) {
                tracingContext.close();
            }
        }
    }

}
