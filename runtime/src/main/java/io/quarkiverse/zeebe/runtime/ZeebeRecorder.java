package io.quarkiverse.zeebe.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;

import io.camunda.zeebe.client.api.command.DeployProcessCommandStep1;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.worker.BackoffSupplier;
import io.camunda.zeebe.client.api.worker.ExponentialBackoffBuilder;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1;
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
    public static List<ZeebeWorkerValue> workers;

    /**
     * Initialize the producer with the configuration
     *
     * @param config zeebe runtime configuration
     */
    public void init(ZeebeRuntimeConfig config) {
        ZeebeClientService client = Arc.container().instance(ZeebeClientService.class).get();
        client.initialize(config);

        if (resources != null && !resources.isEmpty()) {

            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            DeployProcessCommandStep1 cmd = client.newDeployCommand();
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

        if (workers != null && !workers.isEmpty()) {
            //            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            ZeebeWorkerContainer con = Arc.container().instance(ZeebeWorkerContainer.class).get();
            Map<String, JobHandler> c = con.getJobHandlers();
            for (ZeebeWorkerValue w : workers) {
                try {

                    JobHandler jobHandler = c.get(w.clazz);

                    //                    Class<?> clazz = getClassForName(cl, w.clazz);
                    //                    JobHandler jobHandler = (JobHandler) Arc.container().instance(clazz).get();

                    // check the worker type
                    String type = w.type;
                    if (type == null || type.isEmpty()) {
                        type = config.worker.defaultType.orElse(type);
                    }

                    final JobWorkerBuilderStep1.JobWorkerBuilderStep3 builder = client
                            .newWorker()
                            .jobType(type)
                            .handler(jobHandler);

                    // overwrite the annotation with properties
                    ZeebeRuntimeConfig.HandlerConfig hc = config.workers.get(type);
                    if (hc != null) {
                        hc.name.ifPresent(n -> w.name = n);
                        hc.maxJobsActive.ifPresent(n -> w.maxJobsActive = n);
                        hc.timeout.ifPresent(n -> w.timeout = n);
                        hc.pollInterval.ifPresent(n -> w.pollInterval = n);
                        hc.requestTimeout.ifPresent(n -> w.requestTimeout = n);
                        hc.fetchVariables.ifPresent(n -> w.fetchVariables = n.toArray(new String[0]));
                        hc.exponentialBackoff.backoffFactor.ifPresent(n -> w.expBackoffFactor = n);
                        hc.exponentialBackoff.jitterFactor.ifPresent(n -> w.expJitterFactor = n);
                        hc.exponentialBackoff.minDelay.ifPresent(n -> w.expMinDelay = n);
                        hc.exponentialBackoff.maxDelay.ifPresent(n -> w.expMaxDelay = n);
                    }

                    // using defaults from config if null, 0 or negative
                    if (w.name != null && !w.name.isEmpty()) {
                        builder.name(w.name);
                    }

                    if (w.maxJobsActive > 0) {
                        builder.maxJobsActive(w.maxJobsActive);
                    }
                    if (w.timeout > 0) {
                        builder.timeout(w.timeout);
                    }
                    if (w.pollInterval > 0) {
                        builder.pollInterval(Duration.ofMillis(w.pollInterval));
                    }
                    if (w.requestTimeout > 0) {
                        builder.requestTimeout(Duration.ofSeconds(w.requestTimeout));
                    }
                    if (w.fetchVariables.length > 0) {
                        builder.fetchVariables(w.fetchVariables);
                    }

                    // setup ExponentialBackoff configuration
                    if (w.expBackoffFactor > 0 || w.expJitterFactor > 0 || w.expMaxDelay > 0 || w.expMinDelay > 0) {
                        ExponentialBackoffBuilder exp = BackoffSupplier.newBackoffBuilder();
                        if (w.expBackoffFactor > 0) {
                            exp.backoffFactor(w.expBackoffFactor);
                        }
                        if (w.expJitterFactor > 0) {
                            exp.jitterFactor(w.expJitterFactor);
                        }
                        if (w.expMaxDelay > 0) {
                            exp.maxDelay(w.expMaxDelay);
                        }
                        if (w.expMinDelay > 0) {
                            exp.minDelay(w.expMinDelay);
                        }
                        builder.backoffSupplier(exp.build());
                    }

                    builder.open();

                    log.infof("Starting worker %s for job type %s", w.clazz, w.type);
                } catch (Exception e) {
                    log.errorf(e, "Error opening worker for type %s with class %s", w.type, w.clazz);
                }
            }
        }
    }

    public void setResources(Collection<String> resources, List<ZeebeWorkerValue> workers) {
        ZeebeRecorder.resources = resources;
        ZeebeRecorder.workers = new ArrayList<>(workers);
    }

    public static Class<?> getClassForName(ClassLoader cl, String classname) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(classname, false, cl);
        } catch (ClassNotFoundException ignored) {
        }
        log.debugf("getClass: TCCL: %s ## %s : %s", cl, classname, (clazz != null));
        return clazz;
    }
}
