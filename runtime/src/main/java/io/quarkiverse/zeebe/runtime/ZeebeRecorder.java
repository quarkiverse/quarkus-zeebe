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
     * List of paths to the bpmn files in classpath
     */
    private static Collection<String> resources;

    /**
     * Job handler map with job type as key and class name as value
     */
    private static List<JobWorkerMetadata> workers;

    /**
     * Initialize the producer with the configuration
     *
     * @param config zeebe runtime configuration
     */
    public void init(ZeebeRuntimeConfig config) {
        // client configuration
        ZeebeClientService clientService = Arc.container().instance(ZeebeClientService.class).get();
        ZeebeClient client = clientService.client();

        // tracing configuration
        if (config.client.tracing.attributes.isPresent()) {
            List<String> attrs = config.client.tracing.attributes.get();
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
        if (workers != null && !workers.isEmpty()) {

            JobWorkerExceptionHandler handler = Arc.container().instance(JobWorkerExceptionHandler.class).get();
            MetricsRecorder metricsRecorder = Arc.container().instance(MetricsRecorder.class).get();
            TracingRecorder tracingRecorder = Arc.container().instance(TracingRecorder.class).get();

            Set<String> tracingVariables = null;
            Collection<String> fields = tracingRecorder.fields();
            if (fields != null && !fields.isEmpty()) {
                tracingVariables = new HashSet<>(fields);
            }

            for (JobWorkerMetadata meta : workers) {
                try {
                    JobWorkerBuilderStep1.JobWorkerBuilderStep3 builder = buildJobWorker(client, config.client, handler,
                            meta, metricsRecorder, tracingRecorder, tracingVariables);
                    if (builder != null) {
                        clientService.openWorker(builder);
                        log.infof("Starting worker %s.%s for job type %s", meta.declaringClassName, meta.methodName,
                                meta.workerValue.type);
                    }
                } catch (Exception e) {
                    log.errorf(e, "Error opening worker for type %s with class %s.%s", meta.workerValue.type,
                            meta.declaringClassName,
                            meta.methodName);
                }
            }
        }
    }

    private static JobWorkerBuilderStep1.JobWorkerBuilderStep3 buildJobWorker(ZeebeClient client,
            ZeebeClientRuntimeConfig config,
            JobWorkerExceptionHandler exceptionHandler, JobWorkerMetadata meta, MetricsRecorder metricsRecorder,
            TracingRecorder tracingRecorder, Set<String> tracingVariables) {
        JobWorkerValue value = meta.workerValue;

        // check the worker type
        String type = value.type;
        if (type == null || type.isEmpty()) {
            // if configuration default-type is null use method name
            type = config.job.defaultType.orElse(meta.methodName);
        }

        // overwrite the annotation with properties
        ZeebeClientRuntimeConfig.JobHandlerConfig jonHandlerConfig = config.workers.get(type);
        if (jonHandlerConfig != null) {
            jonHandlerConfig.name.ifPresent(n -> value.name = n);
            jonHandlerConfig.enabled.ifPresent(n -> value.enabled = n);
            jonHandlerConfig.maxJobsActive.ifPresent(n -> value.maxJobsActive = n);
            jonHandlerConfig.timeout.ifPresent(n -> value.timeout = n);
            jonHandlerConfig.pollInterval.ifPresent(n -> value.pollInterval = n);
            jonHandlerConfig.requestTimeout.ifPresent(n -> value.requestTimeout = n);
        }

        // skip disabled workers
        if (!value.enabled) {
            log.infof("Job worker %s.%s for job type %s is disabled.", meta.declaringClassName, meta.methodName,
                    value.type);
            return null;
        }

        JobWorkerInvoker invoker = createJobWorkerInvoker(meta.invokerClass);
        JobWorkerHandler jobHandler = new JobWorkerHandler(meta, invoker, metricsRecorder, exceptionHandler,
                config.autoComplete, tracingRecorder);

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
        exp.backoffFactor(config.job.expBackoffFactor);
        exp.jitterFactor(config.job.expJitterFactor);
        exp.maxDelay(config.job.expMaxDelay);
        exp.minDelay(config.job.expMinDelay);
        builder.backoffSupplier(exp.build());

        return builder;
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

    public void setResources(Collection<String> resources, List<JobWorkerMetadata> workers) {
        ZeebeRecorder.resources = resources;
        ZeebeRecorder.workers = new ArrayList<>(workers);
    }

}
