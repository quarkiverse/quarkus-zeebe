package io.quarkiverse.zeebe.opentracing;

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

@ZeebeTraced
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
public class ZeebeOpentracingInterceptor {

    @Inject
    Tracer tracer;

    @AroundInvoke
    public Object wrap(InvocationContext ctx) throws Exception {
        Span span = createSpan(tracer, ctx.getTarget().getClass().getName().replace("_Subclass", ""),
                (ActivatedJob) ctx.getParameters()[1]);
        try (Scope scope = tracer.scopeManager().activate(span)) {
            return ctx.proceed();
        } catch (Throwable e) {
            Tags.ERROR.set(span, true);
            span.setTag("bpmn-worker-exception", e.getMessage());
            throw e;
        } finally {
            span.finish();
        }
    }

    public static Span createSpan(Tracer tracer, String clazz, ActivatedJob job) {
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan("handle")
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);
        SpanContext parentContext = extract(tracer, job.getVariablesAsMap());
        if (parentContext != null) {
            spanBuilder.asChildOf(parentContext);
        }
        Span span = spanBuilder.start();
        span.setTag("bpmn-class", clazz);
        span.setTag("bpmn-component", "zeebe-worker");
        span.setOperationName("handle");
        addTraceInfo(span, job);

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

    private static void addTraceInfo(Span span, ActivatedJob job) {
        span.setOperationName(job.getType());
        span.setTag("bpmn-process-id", job.getBpmnProcessId());
        span.setTag("bpmn-process-instance-key", job.getProcessInstanceKey());
        span.setTag("bpmn-process-element-id", job.getElementId());
        span.setTag("bpmn-process-element-instance-key", job.getElementInstanceKey());
        span.setTag("bpmn-process-def-key", job.getProcessDefinitionKey());
        span.setTag("bpmn-process-def-ver", job.getProcessDefinitionVersion());
    }
}