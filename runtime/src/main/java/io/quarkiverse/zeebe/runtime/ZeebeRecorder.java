package io.quarkiverse.zeebe.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.worker.BackoffSupplier;
import io.camunda.zeebe.client.api.worker.ExponentialBackoffBuilder;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1;
import io.quarkiverse.zeebe.JobWorkerExceptionHandler;
import io.quarkiverse.zeebe.runtime.metrics.MetricsRecorder;
import io.quarkiverse.zeebe.runtime.tracing.TracingRecorder;
import io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ZeebeRecorder {

    private static final Logger log = Logger.getLogger(ZeebeRecorder.class);

    /**
     * Initialize the producer with the configuration
     */
    public void init(ZeebeRuntimeConfig config, Collection<String> resources, List<JobWorkerMetadata> workers) {
        // client configuration
        ZeebeClient client = Arc.container().instance(ZeebeClient.class).get();

        // tracing configuration
        if (config.client().tracing().attributes().isPresent()) {
            List<String> attrs = config.client().tracing().attributes().get();
            if (!attrs.isEmpty()) {
                ZeebeTracing.setAttributes(attrs);
            }
        }

        // deploy resources
        if (resources != null && !resources.isEmpty()) {

            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            DeployResourceCommandStep1 cmd = client.newDeployResourceCommand();
            DeploymentEvent deploymentResult = resources
                    .stream()
                    .flatMap(name -> Stream.of(cl.getResource(name)))
                    .filter(Objects::nonNull)
                    .map(resource -> {
                        try (InputStream inputStream = resource.openStream()) {
                            return cmd.addResourceStream(inputStream, resource.getPath());
                        } catch (IOException e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    })
                    .filter(Objects::nonNull)
                    .reduce((f, s) -> s)
                    .orElseThrow(() -> new IllegalArgumentException("Requires at least one resource to deploy"))
                    .send().join();

            log.infof(
                    "Deployed: %s",
                    deploymentResult
                            .getProcesses()
                            .stream()
                            .map(wf -> String.format("<%s:%d>", wf.getBpmnProcessId(), wf.getVersion()))
                            .collect(Collectors.joining(",")));
        }

        // create job workers
        if (config.client().workersDisabled()) {
            log.infof("Workers are disabled in general and are not started");
        } else if (workers != null && !workers.isEmpty()) {
            JobWorkerExceptionHandler handler = Arc.container().instance(JobWorkerExceptionHandler.class).get();
            MetricsRecorder metricsRecorder = Arc.container().instance(MetricsRecorder.class).get();
            TracingRecorder tracingRecorder = Arc.container().instance(TracingRecorder.class).get();

            Set<String> tracingVariables = null;
            Collection<String> fields = tracingRecorder.fields();
            if (fields != null && !fields.isEmpty()) {
                tracingVariables = new HashSet<>(fields);
            }

            for (JobWorkerMetadata meta : workers) {
                String jobType = getJobType(config.client(), meta);
                try {
                    var jobWorker = buildJobWorker(client, config.client(), handler,
                            meta, metricsRecorder, tracingRecorder, tracingVariables, jobType);
                    if (jobWorker != null) {
                        log.infof("Starting worker %s.%s for job type %s", meta.declaringClassName, meta.methodName,
                                jobType);
                    }
                } catch (Exception e) {
                    log.errorf(e, "Error opening worker for type %s with class %s.%s", jobType,
                            meta.declaringClassName,
                            meta.methodName);
                }
            }
        }
    }

    private static String getJobType(ZeebeClientRuntimeConfig config, JobWorkerMetadata meta) {
        // check the worker type
        String type = meta.workerValue.type;
        if (type == null || type.isEmpty()) {
            // if configuration default-type is null use method name
            return config.job().defaultType().orElse(meta.methodName);
        }
        return type;
    }

    private static JobWorker buildJobWorker(ZeebeClient client,
            ZeebeClientRuntimeConfig config,
            JobWorkerExceptionHandler exceptionHandler, JobWorkerMetadata meta, MetricsRecorder metricsRecorder,
            TracingRecorder tracingRecorder, Set<String> tracingVariables, String type) {
        JobWorkerValue value = meta.workerValue;

        // overwrite the annotation with properties
        ZeebeClientRuntimeConfig.JobHandlerConfig jonHandlerConfig = config.workers().get(type);
        if (jonHandlerConfig != null) {
            jonHandlerConfig.name().ifPresent(n -> value.name = n);
            jonHandlerConfig.enabled().ifPresent(n -> value.enabled = n);
            jonHandlerConfig.maxJobsActive().ifPresent(n -> value.maxJobsActive = n);
            jonHandlerConfig.timeout().ifPresent(n -> value.timeout = n);
            jonHandlerConfig.pollInterval().ifPresent(n -> value.pollInterval = n);
            jonHandlerConfig.requestTimeout().ifPresent(n -> value.requestTimeout = n);
        }

        // skip disabled workers
        if (!value.enabled) {
            log.infof("Job worker %s.%s for job type %s is disabled.", meta.declaringClassName, meta.methodName,
                    value.type);
            return null;
        }

        JobWorkerInvoker invoker = createJobWorkerInvoker(meta.invokerClass);
        JobWorkerHandler jobHandler = new JobWorkerHandler(meta, invoker, metricsRecorder, exceptionHandler,
                config.autoComplete(), tracingRecorder);

        final JobWorkerBuilderStep1.JobWorkerBuilderStep3 builder = client
                .newWorker()
                .jobType(type)
                .handler(jobHandler);

        // using defaults from config if null, 0 or negative
        if (value.name != null && !value.name.isEmpty()) {
            builder.name(value.name);
        }

        if (value.maxJobsActive > 0) {
            builder.maxJobsActive(value.maxJobsActive);
        }
        if (value.timeout > 0) {
            builder.timeout(value.timeout);
        }
        if (value.pollInterval > 0) {
            builder.pollInterval(Duration.ofMillis(value.pollInterval));
        }
        if (value.requestTimeout > 0) {
            builder.requestTimeout(Duration.ofSeconds(value.requestTimeout));
        }

        if (!value.fetchAllVariables) {
            // fetch list of defined variables
            if (value.fetchVariables != null && value.fetchVariables.length > 0) {
                // add tracing variables
                if (tracingVariables != null && !tracingVariables.isEmpty()) {
                    Set<String> tmp = new HashSet<>(tracingVariables);
                    tmp.addAll(Arrays.asList(value.fetchVariables));
                    value.fetchVariables = tmp.toArray(new String[0]);
                }
                // set up the fetch variables
                builder.fetchVariables(value.fetchVariables);
            }
        }

        // setup exponential backoff pull configuration
        ExponentialBackoffBuilder exp = BackoffSupplier.newBackoffBuilder();
        exp.backoffFactor(config.job().expBackoffFactor());
        exp.jitterFactor(config.job().expJitterFactor());
        exp.maxDelay(config.job().expMaxDelay());
        exp.minDelay(config.job().expMinDelay());
        builder.backoffSupplier(exp.build());

        return builder.open();
    }

    private static JobWorkerInvoker createJobWorkerInvoker(String name) {
        try {
            Class<?> invokerClazz = Thread.currentThread().getContextClassLoader().loadClass(name);
            return (JobWorkerInvoker) invokerClazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new IllegalStateException("Unable to create invoker factory: " + name, e);
        }
    }

}
