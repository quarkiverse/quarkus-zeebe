package io.quarkiverse.zeebe.opentelemetry;

import static io.quarkus.opentelemetry.runtime.OpenTelemetryConfig.INSTRUMENTATION_NAME;

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
import io.quarkiverse.zeebe.ZeebeTraced;

@ZeebeTraced
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
public class ZeebeOpenTelemetryInterceptor {

    //    private final Instrumenter<MethodRequest, Void> instrumenter;
    private final OpenTelemetry openTelemetry;

    public ZeebeOpenTelemetryInterceptor(final OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        //        InstrumenterBuilder<MethodRequest, Void> builder = Instrumenter.builder(
        //                openTelemetry,
        //                INSTRUMENTATION_NAME,
        //                new ActivityJobTypeExtractor());
        //
        //        builder.addAttributesExtractor(new AttributesExtractor<>() {
        //            @Override
        //            public void onStart(AttributesBuilder attributes, MethodRequest methodRequest) {
        //                ActivatedJob job = methodRequest.getJob();
        //                attributes.put("bpmn-class", methodRequest.getClazz().replace("_Subclass", ""));
        //                attributes.put("bpmn-component", "zeebe-worker");
        //                attributes.put("bpmn-process-id", job.getBpmnProcessId());
        //                attributes.put("bpmn-process-instance-key", job.getProcessInstanceKey());
        //                attributes.put("bpmn-process-element-id", job.getElementId());
        //                attributes.put("bpmn-process-element-instance-key", job.getElementInstanceKey());
        //                attributes.put("bpmn-process-def-key", job.getProcessDefinitionKey());
        //                attributes.put("bpmn-retries", job.getRetries());
        //                attributes.put("bpmn-process-def-ver", job.getProcessDefinitionVersion());
        //            }
        //
        //            @Override
        //            public void onEnd(AttributesBuilder attributes, MethodRequest methodRequest, @Nullable Void unused,
        //                    @Nullable Throwable error) {
        //                if (error != null) {
        //                    attributes.put("error", true);
        //                    attributes.put("bpmn-worker-exception", error.getMessage());
        //                }
        //            }
        //        });
        //        builder.setSpanStatusExtractor((methodRequest, unused, error) -> {
        //            if (error != null) {
        //                return StatusCode.ERROR;
        //            }
        //            return StatusCode.OK;
        //        });
        //        this.instrumenter = builder
        //                .newConsumerInstrumenter(new ActivityJobGetter());
    }

    @AroundInvoke
    public Object wrap(InvocationContext ctx) throws Exception {

        ActivatedJob job = (ActivatedJob) ctx.getParameters()[1];

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
                return null;
            }
        });

        Span span = openTelemetry.getTracer(INSTRUMENTATION_NAME)
                .spanBuilder(job.getType())
                .setParent(context)
                .setSpanKind(SpanKind.CONSUMER)
                .setAttribute("bpmn-class", ctx.getTarget().getClass().getName().replace("_Subclass", ""))
                .setAttribute("bpmn-component", "zeebe-worker")
                .setAttribute("bpmn-process-id", job.getBpmnProcessId())
                .setAttribute("bpmn-process-instance-key", job.getProcessInstanceKey())
                .setAttribute("bpmn-process-element-id", job.getElementId())
                .setAttribute("bpmn-process-element-instance-key", job.getElementInstanceKey())
                .setAttribute("bpmn-process-def-key", job.getProcessDefinitionKey())
                .setAttribute("bpmn-process-def-ver", job.getProcessDefinitionVersion())
                .setAttribute("bpmn-retries", job.getRetries())
                .startSpan();

        try (Scope ignored = span.makeCurrent()) {
            // Handle request and send response back.
            return ctx.proceed();
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR);
            span.setAttribute("bpmn-worker-exception", t.getMessage());
            throw t;
        } finally {
            span.end();
        }
    }

    //        Context parentContext = Context.current();
    ////        instrumenter.shouldStart(parentContext, methodRequest);
    //        Context spanContext = instrumenter.start(parentContext, methodRequest);
    //
    //        try (Scope scope = spanContext.makeCurrent()) {
    //            Object result = ctx.proceed();
    //            instrumenter.end(spanContext, methodRequest, null, null);
    //            return result;
    //        } catch (Throwable t) {
    //            instrumenter.end(spanContext, methodRequest, null, t);
    //            throw t;
    //        }
    //    }

    //    private static final Tracer tracer = OpenTelemetry.getTracer();
    //    void onRequestReceived() {
    //        ContextPropagators propagators = OpenTelemetry.getPropagators();
    //        TextMapPropagator textMapPropagator = propagators.getTextMapPropagator();
    //
    //        // Extract and store the propagated span's SpanContext and other available concerns
    //        // in the specified Context.
    //        Context context = textMapPropagator.extract(Context.current(), request,
    //                new Getter<String, String>() {
    //                    public String get(Object request, String key) {
    //                        // Return the value associated to the key, if available.
    //                    }
    //                }
    //        );
    //        Span span = tracer.spanBuilder("MyRequest")
    //                .setParent(context)
    //                .setSpanKind(SpanKind.SERVER).startSpan();
    //        try (Scope ignored = span.makeCurrent()) {
    //            // Handle request and send response back.
    //        } finally {
    //            span.end();
    //        }
    //    }
    //
    //    private static final class ActivityJobTypeExtractor implements SpanNameExtractor<MethodRequest> {
    //        @Override
    //        public String extract(final MethodRequest methodRequest) {
    //            return methodRequest.getJob().getType();
    //        }
    //    }
    //
    //    private static final class ActivityJobGetter implements TextMapGetter<MethodRequest> {
    //
    //        @Override
    //        public Iterable<String> keys(MethodRequest carrier) {
    //            return carrier.getVariables().keySet();
    //        }
    //
    //        @Nullable
    //        @Override
    //        public String get(@Nullable MethodRequest carrier, String key) {
    //            Object o = carrier.getVariables().get(key);
    //            if (o instanceof String) {
    //                return (String) o;
    //            }
    //            return null;
    //        }
    //    }

}
