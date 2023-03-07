package io.quarkiverse.zeebe.runtime.tracing;

import static io.quarkus.opentelemetry.runtime.config.OpenTelemetryConfig.INSTRUMENTATION_NAME;
import static java.lang.String.valueOf;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;

@ApplicationScoped
public class OpenTelemetryTracingRecorder implements TracingRecorder {

    @Inject
    OpenTelemetry openTelemetry;

    @Override
    public TracingContext createTracingContext(String clazz, String method, String name, ActivatedJob job) {
        Span span = createSpan(openTelemetry, clazz, method, name, job);
        Scope scope = span.makeCurrent();
        return new OpenTelemetryTracingContext(span, scope);
    }

    @Override
    public Collection<String> fields() {
        return openTelemetry.getPropagators().getTextMapPropagator().fields();
    }

    public static class OpenTelemetryTracingContext implements TracingContext {

        Span span;

        Scope scope;

        public OpenTelemetryTracingContext(Span span, Scope scope) {
            this.span = span;
            this.scope = scope;
        }

        @Override
        public void close() {
            scope.close();
            span.end();
        }

        @Override
        public void error(String key, Throwable t) {
            span.setStatus(StatusCode.ERROR);
            span.setAttribute(key, t.getMessage());
        }

        @Override
        public void ok() {
            span.setStatus(StatusCode.OK);
        }
    }

    private static Span createSpan(OpenTelemetry openTelemetry, String clazz, String method, String spanName,
            ActivatedJob job) {
        TextMapPropagator textMapPropagator = openTelemetry.getPropagators().getTextMapPropagator();

        Context context = textMapPropagator.extract(Context.current(), job.getVariablesAsMap(), new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(Map<String, Object> data) {
                return data.keySet();
            }

            @Nullable
            @Override
            public String get(@Nullable Map<String, Object> data, String key) {
                if (data == null) {
                    return null;
                }
                Object o = data.get(key);
                if (o instanceof String) {
                    return (String) o;
                }
                return valueOf(o);
            }
        });

        Span span = openTelemetry.getTracer(INSTRUMENTATION_NAME).spanBuilder(spanName).setParent(context)
                .setSpanKind(SpanKind.CONSUMER).startSpan();

        ZeebeTracing.setAttributes(clazz, method, job, new ZeebeTracing.AttributeConfigCallback() {
            @Override
            public void setAttribute(String key, long value) {
                span.setAttribute(key, value);
            }

            @Override
            public void setAttribute(String key, String value) {
                span.setAttribute(key, value);
            }
        });

        return span;
    }

}
