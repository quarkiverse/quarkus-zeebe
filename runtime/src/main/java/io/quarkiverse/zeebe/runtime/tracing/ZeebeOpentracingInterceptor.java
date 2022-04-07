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
import io.quarkiverse.zeebe.ZeebeTraced;

@ZeebeTraced
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
public class ZeebeOpentracingInterceptor {

    @Inject
    Tracer tracer;

    @AroundInvoke
    public Object wrap(InvocationContext ctx) throws Exception {
        Span span = createSpan(tracer, ZeebeTracing.getName(ctx.getTarget().getClass()),
                (ActivatedJob) ctx.getParameters()[1]);
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

    private static Span createSpan(Tracer tracer, String clazz, ActivatedJob job) {
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(job.getType())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);
        SpanContext parentContext = extract(tracer, job.getVariablesAsMap());
        if (parentContext != null) {
            spanBuilder.asChildOf(parentContext);
        }
        spanBuilder.withTag(ZeebeTracing.CLASS, clazz);
        spanBuilder.withTag(ZeebeTracing.COMPONENT, ZeebeTracing.COMPONENT_NAME);
        spanBuilder.withTag(ZeebeTracing.PROCESS_ID, job.getBpmnProcessId());
        spanBuilder.withTag(ZeebeTracing.PROCESS_INSTANCE_KEY, job.getProcessInstanceKey());
        spanBuilder.withTag(ZeebeTracing.PROCESS_ELEMENT_ID, job.getElementId());
        spanBuilder.withTag(ZeebeTracing.PROCESS_ELEMENT_INSTANCE_KEY, job.getElementInstanceKey());
        spanBuilder.withTag(ZeebeTracing.PROCESS_DEF_KEY, job.getProcessDefinitionKey());
        spanBuilder.withTag(ZeebeTracing.PROCESS_DEF_VER, job.getProcessDefinitionVersion());
        spanBuilder.withTag(ZeebeTracing.RETRIES, job.getRetries());
        return spanBuilder.start();
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