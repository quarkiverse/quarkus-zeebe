package io.quarkiverse.zeebe.runtime.tracing;

import javax.inject.Singleton;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;

import java.util.Collection;

@Singleton
@Unremovable
@DefaultBean
public class DefaultTracingRecorder implements TracingRecorder {

    private static final TracingRecorder.TracingContext CONTEXT = new TracingContext() {

        @Override
        public void error(String key, Throwable t) {

        }

        @Override
        public void close() {

        }

        @Override
        public void ok() {

        }
    };

    @Override
    public Collection<String> fields() {
        return null;
    }

    @Override
    public TracingContext createTracingContext(String clazz, String method, String name, ActivatedJob job) {
        return CONTEXT;
    }
}
