package io.quarkiverse.zeebe.runtime;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableContext;
import io.quarkus.arc.ManagedContext;

public class JobWorkerInvoker {

    public CompletionStage<Object> invoke(JobClient client, ActivatedJob job) throws Exception {
        ManagedContext requestContext = Arc.container().requestContext();
        if (requestContext.isActive()) {
            return invokeBean(client, job);
        } else {
            // 1. Activate the context
            // 2. Capture the state (which is basically a shared Map instance)
            // 3. Destroy the context correctly when the returned stage completes
            requestContext.activate();
            final InjectableContext.ContextState state = requestContext.getState();
            try {
                return invokeBean(client, job).whenComplete((v, t) -> {
                    requestContext.destroy(state);
                });
            } catch (RuntimeException e) {
                // Just terminate the context and rethrow the exception if something goes really wrong
                requestContext.terminate();
                throw e;
            } finally {
                // Always deactivate the context
                requestContext.deactivate();
            }
        }
    }

    protected CompletionStage<Object> invokeBean(JobClient client, ActivatedJob job) throws Exception {
        return null;
    }

    /**
     * A blocking invoker is executed on the main executor for blocking tasks.
     * A non-blocking invoker is executed on the event loop.
     *
     * @return {@code true} if the scheduled method is blocking, {@code false} otherwise
     */
    protected boolean isBlocking() {
        return true;
    }

    protected Object getVariable(ActivatedJob job, String name, Class<?> clazz) {
        Object value = job.getVariablesAsMap().get(name);
        try {
            if (value != null && !clazz.isInstance(value)) {
                // get default zeebe json mapper
                JsonMapper mapper = Arc.container().instance(JsonMapper.class).get();
                String tmp = mapper.toJson(value);
                return mapper.fromJson(tmp, clazz);
            } else {
                return clazz.cast(value);
            }
        } catch (ClassCastException | IllegalArgumentException ex) {
            throw new RuntimeException(
                    "Cannot assign process variable '" + name + "' to parameter of type '" + clazz + "' when executing job '"
                            + job.getType() + "', invalid type found: " + ex.getMessage());
        }
    }

    protected Object getVariablesAsType(ActivatedJob job, Class<?> clazz) {
        try {
            return job.getVariablesAsType(clazz);
        } catch (RuntimeException e) {
            throw new RuntimeException("Cannot assign process variables to type '" + clazz.getName() + "' when executing job '"
                    + job.getType() + "', cause is: " + e.getMessage(), e);
        }
    }

    protected String getCustomHeader(ActivatedJob job, String name) {
        return job.getCustomHeaders().get(name);
    }

    protected Map<String, String> getCustomHeaders(ActivatedJob job) {
        return job.getCustomHeaders();
    }
}
