package io.quarkiverse.zeebe.runtime.tracing;

import io.camunda.zeebe.client.api.response.ActivatedJob;

import java.util.Collection;

public interface TracingRecorder {

    TracingContext createTracingContext(String clazz, String method, String name, ActivatedJob job);

    Collection<String> fields();

    interface TracingContext {

        void error(String key, Throwable t);

        void close();

        void ok();
    }

}
