package io.quarkiverse.zeebe.runtime;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.quarkus.arc.Arc;
import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle;
import io.smallrye.common.vertx.VertxContext;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * Invokes a job worker business method of a bean.
 */
public class JobWorkerHandler implements JobHandler {

    private static final Logger log = LoggerFactory.getLogger(JobWorkerHandler.class);

    private JobWorkerMetadata jobWorkerMetadata;

    private JobWorkerInvoker invoker;

    private JobWorkerExceptionHandler exceptionHandler;

    private BackoffSupplier backoffSupplier;

    private ZeebeClientRuntimeConfig.AutoCompleteConfig autoCompleteConfig;

    private MetricsRecorder metricsRecorder;

    private TracingRecorder tracingRecorder;

    private String spanName;

    public JobWorkerHandler(JobWorkerMetadata jobWorkerMetadata, JobWorkerInvoker invoker,
            MetricsRecorder metricsRecorder,
            JobWorkerExceptionHandler exceptionHandler, ZeebeClientRuntimeConfig.AutoCompleteConfig autoCompleteConfig,
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
        log.trace("Handle {} and invoke worker {}", job, jobWorkerMetadata.workerValue);

        Vertx vertx = Arc.container().instance(Vertx.class).get();
        Context context = VertxContext.getOrCreateDuplicatedContext(vertx);
        VertxContextSafetyToggle.setContextSafe(context, true);
        if (invoker.isBlocking()) {
            context.executeBlocking(p -> {
                try {
                    doInvoke(client, job);
                } finally {
                    p.complete();
                }
            }, false);
        } else {
            context.runOnContext(event -> doInvoke(client, job));
        }
    }

    private void doInvoke(JobClient client, ActivatedJob job) {

        try {
            TracingRecorder.TracingContext tracingContext = tracingRecorder
                    .createTracingContext(jobWorkerMetadata.declaringClassName, jobWorkerMetadata.methodName, spanName, job);

            metricsRecorder.increase(MetricsRecorder.METRIC_NAME_JOB, MetricsRecorder.ACTION_ACTIVATED, job.getType());

            invoker.invoke(client, job)
                    .thenApply(result -> {
                        // check the auto-complete
                        // the complete muss close tracing context
                        if (jobWorkerMetadata.workerValue.autoComplete) {
                            JobWorkerCommand.createJobWorkerCommand(client, job, result)
                                    .request(tracingContext, metricsRecorder, backoffSupplier, exceptionHandler,
                                            autoCompleteConfig.maxRetries, autoCompleteConfig.retryDelay)
                                    .send();
                        }
                        return result;
                    })
                    .exceptionally(ex -> {

                        try {
                            // update metrics
                            metricsRecorder.increase(MetricsRecorder.METRIC_NAME_JOB, MetricsRecorder.ACTION_FAILED,
                                    job.getType());
                            // switch tracing to error
                            tracingContext.error(ZeebeTracing.JOB_EXCEPTION, ex);

                            // check the completion exception
                            if (ex instanceof CompletionException) {
                                ex = ex.getCause();
                            }

                            // throw error for ZeebeBpmnError exception
                            // the throw error muss close tracing context
                            if (ex instanceof ZeebeBpmnError) {
                                log.info("Caught JobWorker BPMN error on {}", job);
                                JobWorkerCommand.createThrowErrorCommand(client, job, (ZeebeBpmnError) ex)
                                        .request(tracingContext, metricsRecorder, backoffSupplier, exceptionHandler,
                                                autoCompleteConfig.maxRetries, autoCompleteConfig.retryDelay)
                                        .send();
                                return null;
                            }
                        } catch (Throwable t) {
                            tracingContext.close();
                            throw t;
                        }

                        // create failed command for the exception, put stack-trace in the error message
                        try {
                            log.info("Caught exception {} error on {}", ex.getMessage(), job);
                            handleException(client, job, ex);
                        } finally {
                            // always close the tracing context
                            tracingContext.close();
                        }
                        return null;
                    });
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }

    private void handleException(JobClient jobClient, ActivatedJob job, Throwable e) {
        log.warn(
                "Worker {} failed to handle job with key {} of type {}, sending fail command to broker",
                job.getWorker(), job.getKey(), job.getType(), e);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        final String message = stringWriter.toString();

        jobClient.newFailCommand(job.getKey())
                .retries(job.getRetries() - 1)
                .errorMessage(message)
                .send();
    }
}
