package io.quarkiverse.zeebe.runtime.tracing;

import java.util.Collection;

import io.camunda.zeebe.client.api.response.ActivatedJob;

public interface TracingRecorder {

    TracingContext createTracingContext(String clazz, String method, String name, ActivatedJob job);

    Collection<String> fields();

    interface TracingContext {

        void error(String key, Throwable t);

        void close();

        void ok();
    }

}
