package io.quarkiverse.zeebe.runtime.tracing;

import static java.lang.String.valueOf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;

@SuppressWarnings("CdiInterceptorInspection")
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
public class ZeebeOpentracingInterceptor {

    @Inject
    Tracer tracer;

    @AroundInvoke
    public Object wrap(InvocationContext ctx) throws Exception {
        ActivatedJob job = (ActivatedJob) ctx.getParameters()[1];
        Span span = createSpan(tracer, ZeebeTracing.getClass(ctx.getTarget().getClass()),
                ZeebeTracing.getSpanName(job, ctx.getMethod()),
                job);
        try (Scope scope = tracer.scopeManager().activate(span)) {
            return ctx.proceed();
        } catch (Throwable e) {
            Tags.ERROR.set(span, true);
            span.setTag(ZeebeTracing.WORKER_EXCEPTION, e.getMessage());
            throw e;
        } finally {
            span.finish();
        }
    }

    private static Span createSpan(Tracer tracer, String clazz, String spanName, ActivatedJob job) {
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(spanName)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);
        SpanContext parentContext = extract(tracer, job.getVariablesAsMap());
        if (parentContext != null) {
            spanBuilder.asChildOf(parentContext);
        }
        Span span = spanBuilder.start();
        ZeebeTracing.setAttributes(clazz, job, new ZeebeTracing.AttributeCallback() {
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