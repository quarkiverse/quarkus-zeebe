package io.quarkiverse.zeebe.runtime.tracing;

import java.util.Map;

import javax.inject.Inject;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.MethodDescriptor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
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

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void sendMessage(ReqT message) {
                Span span = Span.current();
                if (span == null) {
                    super.sendMessage(message);
                    return;
                }

                if (message instanceof GatewayOuterClass.CreateProcessInstanceRequest) {
                    super.sendMessage(createProcessInstance(message));
                    return;
                }

                if (message instanceof GatewayOuterClass.FailJobRequest) {
                    GatewayOuterClass.FailJobRequest request = (GatewayOuterClass.FailJobRequest) message;
                    span.setStatus(StatusCode.ERROR)
                            .setAttribute(ZeebeTracing.FAIL_MESSAGE, request.getErrorMessage().substring(0, 10));
                }
                if (message instanceof GatewayOuterClass.ThrowErrorRequest) {
                    GatewayOuterClass.ThrowErrorRequest request = (GatewayOuterClass.ThrowErrorRequest) message;
                    span.setStatus(StatusCode.ERROR)
                            .setAttribute(ZeebeTracing.THROW_ERROR_MESSAGE, request.getErrorMessage())
                            .setAttribute(ZeebeTracing.THROW_ERROR_CODE, request.getErrorCode());
                }
                super.sendMessage(message);
            }
        };
    }

    private <ReqT> ReqT createProcessInstance(ReqT message) {
        GatewayOuterClass.CreateProcessInstanceRequest.Builder builder = GatewayOuterClass.CreateProcessInstanceRequest
                .newBuilder((GatewayOuterClass.CreateProcessInstanceRequest) message);

        Map<String, Object> variables = mapper.fromJsonAsMap(builder.getVariables());

        ContextPropagators propagators = openTelemetry.getPropagators();
        TextMapPropagator textMapPropagator = propagators.getTextMapPropagator();

        textMapPropagator.inject(Context.current(), variables, Map::put);
        builder.setVariables(mapper.toJson(variables));

        @SuppressWarnings("unchecked")
        ReqT request = (ReqT) builder.build();
        return request;
    }

}
