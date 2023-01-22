package io.quarkiverse.zeebe.runtime.tracing;

import static io.quarkiverse.zeebe.runtime.tracing.ZeebeTracing.CLIENT_EXCEPTION;
import static io.quarkus.opentelemetry.runtime.config.OpenTelemetryConfig.INSTRUMENTATION_NAME;

import java.util.Map;

import javax.inject.Inject;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.quarkiverse.zeebe.ZeebeClientInterceptor;

public class ZeebeOpenTelemetryClientInterceptor implements ZeebeClientInterceptor {

    private final OpenTelemetry openTelemetry;

    @Inject
    JsonMapper mapper;

    public ZeebeOpenTelemetryClientInterceptor(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
            Channel next) {

        Span span = Span.current();
        // ignore if span is not activate
        if (span == null) {
            return next.newCall(method, callOptions);
        }

        return new ZeebeForwardingClient<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            protected void createTracingMessage(ReqT message) {
                // create new span for the request
                Span callSpan = openTelemetry.getTracer(INSTRUMENTATION_NAME)
                        .spanBuilder(message.getClass().getSimpleName())
                        .setSpanKind(SpanKind.CLIENT).startSpan();
                try (Scope ignored = callSpan.makeCurrent()) {
                    sendTracingMessage(message, callback(span), callback(callSpan));
                } catch (Throwable t) {
                    callSpan.setStatus(StatusCode.ERROR).setAttribute(CLIENT_EXCEPTION, t.getMessage());
                    throw t;
                } finally {
                    callSpan.end();
                }
            }

            @Override
            protected GatewayOuterClass.CreateProcessInstanceRequest convert(
                    GatewayOuterClass.CreateProcessInstanceRequest request) {
                GatewayOuterClass.CreateProcessInstanceRequest.Builder builder = GatewayOuterClass.CreateProcessInstanceRequest
                        .newBuilder(request);

                Map<String, Object> variables = mapper.fromJsonAsMap(request.getVariables());
                ContextPropagators propagators = openTelemetry.getPropagators();
                TextMapPropagator textMapPropagator = propagators.getTextMapPropagator();

                textMapPropagator.inject(Context.current(), variables, Map::put);
                builder.setVariables(mapper.toJson(variables));
                return builder.build();
            }
        };
    }

    private static ZeebeForwardingClient.AttributeCallback callback(Span span) {
        return new OpenTelemetryAttributeCallback(span);
    }

    private static final class OpenTelemetryAttributeCallback implements ZeebeForwardingClient.AttributeCallback {

        private final Span span;

        OpenTelemetryAttributeCallback(Span span) {
            this.span = span;
        }

        @Override
        public ZeebeForwardingClient.AttributeCallback setError() {
            this.span.setStatus(StatusCode.ERROR);
            return this;
        }

        @Override
        public ZeebeForwardingClient.AttributeCallback setAttribute(String key, String value) {
            this.span.setAttribute(key, value);
            return this;
        }

        @Override
        public ZeebeForwardingClient.AttributeCallback setAttribute(String key, int value) {
            this.span.setAttribute(key, value);
            return this;
        }

        @Override
        public ZeebeForwardingClient.AttributeCallback setAttribute(String key, long value) {
            this.span.setAttribute(key, value);
            return this;
        }

        @Override
        public ZeebeForwardingClient.AttributeCallback setAttribute(String key, boolean value) {
            this.span.setAttribute(key, value);
            return this;
        }
    }
}
