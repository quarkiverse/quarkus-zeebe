package io.quarkiverse.zeebe.runtime.tracing;

import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.MethodDescriptor;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import io.quarkiverse.zeebe.ZeebeClientInterceptor;

public class ZeebeOpentracingClientInterceptor implements ZeebeClientInterceptor {

    @Inject
    JsonMapper mapper;

    @Inject
    Tracer tracer;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void sendMessage(ReqT message) {

                if (tracer == null) {
                    super.sendMessage(message);
                    return;
                }

                if (message instanceof GatewayOuterClass.CreateProcessInstanceRequest) {
                    super.sendMessage(createProcessInstance(message));
                    return;
                }

                if (message instanceof GatewayOuterClass.FailJobRequest) {
                    failJobRequest(message);
                }
                if (message instanceof GatewayOuterClass.ThrowErrorRequest) {
                    throwErrorRequest(message);
                }
                super.sendMessage(message);
            }
        };
    }

    private <ReqT> void throwErrorRequest(ReqT message) {
        GatewayOuterClass.ThrowErrorRequest request = (GatewayOuterClass.ThrowErrorRequest) message;
        tracer.activeSpan().setTag(Tags.ERROR.getKey(), true)
                .setTag(ZeebeTracing.THROW_ERROR_MESSAGE, request.getErrorMessage())
                .setTag(ZeebeTracing.THROW_ERROR_CODE, request.getErrorCode());
    }

    private <ReqT> void failJobRequest(ReqT message) {
        GatewayOuterClass.FailJobRequest request = (GatewayOuterClass.FailJobRequest) message;
        tracer.activeSpan().setTag(Tags.ERROR.getKey(), true)
                .setTag(ZeebeTracing.FAIL_MESSAGE, request.getErrorMessage().substring(0, 10));
    }

    private <ReqT> ReqT createProcessInstance(ReqT message) {
        GatewayOuterClass.CreateProcessInstanceRequest.Builder builder = GatewayOuterClass.CreateProcessInstanceRequest
                .newBuilder((GatewayOuterClass.CreateProcessInstanceRequest) message);

        Map<String, Object> variables = mapper.fromJsonAsMap(builder.getVariables());
        tracer.inject(tracer.activeSpan().context(), Format.Builtin.TEXT_MAP, new TextMap() {

            @Override
            public void put(String key, String value) {
                variables.put(key, value);
            }

            @Override
            public Iterator<Map.Entry<String, String>> iterator() {
                throw new UnsupportedOperationException("iterator should never be used with Tracer.inject()");
            }
        });
        builder.setVariables(mapper.toJson(variables));

        @SuppressWarnings("unchecked")
        ReqT request = (ReqT) builder.build();
        return request;
    }
}
