package io.quarkiverse.zeebe.runtime.tracing;

import static io.quarkus.opentelemetry.runtime.OpenTelemetryConfig.INSTRUMENTATION_NAME;
import static java.lang.String.valueOf;

import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;

@SuppressWarnings("CdiInterceptorInspection")
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
public class ZeebeOpenTelemetryInterceptor {

    private final OpenTelemetry openTelemetry;

    public ZeebeOpenTelemetryInterceptor(final OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @AroundInvoke
    public Object wrap(InvocationContext ctx) throws Exception {
        ActivatedJob job = (ActivatedJob) ctx.getParameters()[1];
        Span span = createSpan(openTelemetry, ZeebeTracing.getClass(ctx.getTarget().getClass()),
                ZeebeTracing.getSpanName(job, ctx.getMethod()), job);
        try (Scope ignored = span.makeCurrent()) {
            return ctx.proceed();
        } catch (Throwable e) {
            span.setStatus(StatusCode.ERROR);
            span.setAttribute(ZeebeTracing.JOB_EXCEPTION, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    private static Span createSpan(OpenTelemetry openTelemetry, String clazz, String spanName, ActivatedJob job) {
        ContextPropagators propagators = openTelemetry.getPropagators();
        TextMapPropagator textMapPropagator = propagators.getTextMapPropagator();

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

        ZeebeTracing.setAttributes(clazz, job, new ZeebeTracing.AttributeConfigCallback() {
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
