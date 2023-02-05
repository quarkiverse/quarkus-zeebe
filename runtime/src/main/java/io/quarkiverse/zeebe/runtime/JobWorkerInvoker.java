package io.quarkiverse.zeebe.runtime;

import java.util.concurrent.CompletionStage;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableContext;
import io.quarkus.arc.ManagedContext;

public abstract class JobWorkerInvoker {

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

    /**
     * A blocking invoker is executed on the main executor for blocking tasks.
     * A non-blocking invoker is executed on the event loop.
     *
     * @return {@code true} if the scheduled method is blocking, {@code false} otherwise
     */
    protected abstract CompletionStage<Object> invokeBean(JobClient client, ActivatedJob job);

    /**
     * A blocking invoker is executed on the main executor for blocking tasks.
     * A non-blocking invoker is executed on the event loop.
     *
     * @return {@code true} if the scheduled method is blocking, {@code false} otherwise
     */
    protected boolean isBlocking() {
        return true;
    }

}
