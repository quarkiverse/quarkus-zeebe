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
import io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ZeebeRecorder {

    private static final Logger log = Logger.getLogger(ZeebeRecorder.class);

    /**
     * List of paths to the bpmn files in classpath
     */
    public static Collection<String> resources;

    /**
     * Job handler map with job type as key and class name as value
     */
    public static List<JobWorkerMetadata> workers;

    /**
     * Job handler invoker factory class.
     */
    public static String factory;

    /**
     * Initialize the producer with the configuration
     *
     * @param config zeebe runtime configuration
     */
    public void init(ZeebeRuntimeConfig config) {
        // client configuration
        ZeebeClientService clientService = Arc.container().instance(ZeebeClientService.class).get();
        clientService.initialize(config);
        ZeebeClient client = clientService.client();

        // tracing configuration
        if (config.tracing.attributes.isPresent()) {
            List<String> attrs = config.tracing.attributes.get();
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
            JobWorkerInvokerFactory factory = createJobWorkerInvokerFactory(ZeebeRecorder.factory);

            for (JobWorkerMetadata meta : workers) {
                try {
                    JobWorkerBuilderStep1.JobWorkerBuilderStep3 builder = buildJobWorker(client, config, factory, handler,
                            meta);
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

    private static JobWorkerBuilderStep1.JobWorkerBuilderStep3 buildJobWorker(ZeebeClient client, ZeebeRuntimeConfig config,
            JobWorkerInvokerFactory factory, JobWorkerExceptionHandler exceptionHandler, JobWorkerMetadata meta) {
        JobWorkerValue value = meta.workerValue;

        // check the worker type
        String type = value.type;
        if (type == null || type.isEmpty()) {
            type = config.job.defaultType.orElse(type);
        }

        // overwrite the annotation with properties
        ZeebeRuntimeConfig.JobHandlerConfig jonHandlerConfig = config.workers.get(type);
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

        JobWorkerInvoker invoker = factory.create(meta.invokerClass);
        JobWorkerHandler jobHandler = new JobWorkerHandler(value, invoker, exceptionHandler, config.autoComplete);

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
        if (value.fetchVariables.length > 0) {
            builder.fetchVariables(value.fetchVariables);
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

    private static long getConfigValueLong(long annotationValue, Optional<Long> itemConfig, Optional<Long> globalConfig) {
        return itemConfig.orElseGet(() -> {
            if (annotationValue > 0) {
                return annotationValue;
            }
            return globalConfig.orElse(annotationValue);
        });
    }

    private static double getConfigValueDouble(double annotationValue, Optional<Double> itemConfig,
            Optional<Double> globalConfig) {
        return itemConfig.orElseGet(() -> {
            if (annotationValue > 0) {
                return annotationValue;
            }
            return globalConfig.orElse(annotationValue);
        });
    }

    private static JobWorkerInvokerFactory createJobWorkerInvokerFactory(String name) {
        try {
            Class<?> invokerClazz = Thread.currentThread().getContextClassLoader().loadClass(name);
            return (JobWorkerInvokerFactory) invokerClazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new IllegalStateException("Unable to create invoker factory: " + name, e);
        }
    }

    public void setResources(Collection<String> resources, List<JobWorkerMetadata> workers, String factory) {
        ZeebeRecorder.resources = resources;
        ZeebeRecorder.factory = factory;
        ZeebeRecorder.workers = new ArrayList<>(workers);
    }

}
