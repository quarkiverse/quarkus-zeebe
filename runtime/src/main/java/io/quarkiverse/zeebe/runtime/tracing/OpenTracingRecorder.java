package io.quarkiverse.zeebe.runtime.tracing;

import static java.lang.String.valueOf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;

@ApplicationScoped
public class OpenTracingRecorder implements TracingRecorder {

    @Inject
    Tracer tracer;

    @Override
    public Collection<String> fields() {
        //TODO: return open tracing fields
        return null;
    }

    @Override
    public TracingContext createTracingContext(String clazz, String method, String name, ActivatedJob job) {
        Span span = createSpan(tracer, clazz, method, name, job);
        Scope scope = tracer.scopeManager().activate(span);
        return new OpenTracingRecorder.OpenTracingContext(span, scope);
    }

    public static class OpenTracingContext implements TracingContext {

        Span span;

        Scope scope;

        public OpenTracingContext(Span span, Scope scope) {
            this.scope = scope;
            this.span = span;
        }

        @Override
        public void close() {
            scope.close();
            span.finish();
        }

        @Override
        public void error(String key, Throwable t) {
            span.setTag(Tags.ERROR, true)
                    .setTag(key, t.getMessage());
        }

        @Override
        public void ok() {
            span.setTag(Tags.ERROR, false);
        }
    }

    private static Span createSpan(Tracer tracer, String clazz, String method, String spanName, ActivatedJob job) {
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);
        SpanContext parentContext = extract(tracer, job.getVariablesAsMap());
        if (parentContext != null) {
            spanBuilder.asChildOf(parentContext);
        }
        Span span = spanBuilder.start();
        ZeebeTracing.setAttributes(clazz, method, job, new ZeebeTracing.AttributeConfigCallback() {
            @Override
            public void setAttribute(String key, long value) {
                span.setTag(key, value);
            }

            @Override
            public void setAttribute(String key, String value) {
                span.setTag(key, value);
            }
        });
        return span;
    }

    private static SpanContext extract(Tracer tracer, Map<String, Object> parameters) {

        final Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            map.put(entry.getKey(), valueOf(entry.getValue()));
        }
        SpanContext spanContext = tracer
                .extract(Format.Builtin.TEXT_MAP, new TextMap() {

                    @Override
                    public void put(String key, String value) {
                        throw new UnsupportedOperationException(
                                "HeadersMapExtractAdapter should only be used with Tracer.extract()");
                    }

                    @Override
                    public Iterator<Map.Entry<String, String>> iterator() {
                        return map.entrySet().iterator();
                    }
                });
        if (spanContext != null) {
            return spanContext;
        }

        Span span = tracer.activeSpan();
        if (span != null) {
            return span.context();
        }
        return null;
    }

}
