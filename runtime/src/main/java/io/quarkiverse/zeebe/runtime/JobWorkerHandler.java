package io.quarkiverse.zeebe.runtime;

import org.jboss.logging.Logger;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.BackoffSupplier;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.impl.worker.ExponentialBackoffBuilderImpl;
import io.quarkiverse.zeebe.JobWorkerCommand;
import io.quarkiverse.zeebe.JobWorkerExceptionHandler;
import io.quarkiverse.zeebe.ZeebeBpmnError;
import io.quarkus.arc.Unremovable;

/**
 * Invokes a job worker business method of a bean.
 */
@Unremovable
public class JobWorkerHandler implements JobHandler {

    private static final Logger LOG = Logger.getLogger(JobWorkerHandler.class);

    private JobWorkerValue jobWorkerValue;

    private JobWorkerInvoker invoker;

    private JobWorkerExceptionHandler exceptionHandler;

    private BackoffSupplier backoffSupplier;

    private ZeebeRuntimeConfig.AutoCompleteConfig autoCompleteConfig;

    public JobWorkerHandler(JobWorkerValue jobWorkerValue, JobWorkerInvoker invoker,
            JobWorkerExceptionHandler exceptionHandler, ZeebeRuntimeConfig.AutoCompleteConfig autoCompleteConfig) {
        this.jobWorkerValue = jobWorkerValue;
        this.invoker = invoker;
        this.exceptionHandler = exceptionHandler;
        this.autoCompleteConfig = autoCompleteConfig;

        if (jobWorkerValue.autoComplete) {
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
        LOG.tracef("Handle %s and invoke worker %s", job, jobWorkerValue);
        try {
            //TODO: metrics
            Object result = null;
            try {
                result = invoker.invoke(client, job).toCompletableFuture().get();
            } catch (Throwable t) {
                //TODO: metrics
                throw t;
            }

            if (jobWorkerValue.autoComplete) {
                //TODO: metrics
                JobWorkerCommand.createJobWorkerCommand(client, job, result)
                        .backoffSupplier(backoffSupplier)
                        .exceptionHandler(exceptionHandler)
                        .maxRetries(autoCompleteConfig.maxRetries)
                        .retryDelay(autoCompleteConfig.retryDelay)
                        .send();
            }
        } catch (ZeebeBpmnError error) {
            LOG.tracef("Caught JobWorker BPMN error on %s", job);
            //TODO: metrics
            JobWorkerCommand.createJobWorkerCommand(client, job, error)
                    .backoffSupplier(backoffSupplier)
                    .exceptionHandler(exceptionHandler)
                    .maxRetries(autoCompleteConfig.maxRetries)
                    .retryDelay(autoCompleteConfig.retryDelay)
                    .send();
        }
    }

}
