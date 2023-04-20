package io.quarkiverse.zeebe.runtime.tracing;

import java.util.Iterator;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import io.quarkiverse.zeebe.ZeebeClientInterceptor;

@ApplicationScoped
public class ZeebeOpenTracingClientInterceptor implements ZeebeClientInterceptor {

    @Inject
    JsonMapper mapper;

    @Inject
    Tracer tracer;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
            Channel next) {

        // ignore if span is not activate
        if (tracer == null) {
            return next.newCall(method, callOptions);
        }

        return new ZeebeForwardingClient<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            protected void createTracingMessage(ReqT message) {
                Span span = tracer.activeSpan();
                Span callSpan = tracer.buildSpan(message.getClass().getSimpleName())
                        .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                        .asChildOf(span).start();
                try (Scope scope = tracer.scopeManager().activate(callSpan)) {
                    sendTracingMessage(message, callback(span), callback(callSpan));
                } catch (Throwable e) {
                    callSpan.setTag(Tags.ERROR, true).setTag(ZeebeTracing.JOB_EXCEPTION, e.getMessage());
                    throw e;
                } finally {
                    callSpan.finish();
                }
            }

            @Override
            GatewayOuterClass.CreateProcessInstanceRequest convert(GatewayOuterClass.CreateProcessInstanceRequest request) {
                GatewayOuterClass.CreateProcessInstanceRequest.Builder builder = GatewayOuterClass.CreateProcessInstanceRequest
                        .newBuilder(request);
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
                return builder.build();
            }
        };

    }

    private static ZeebeForwardingClient.AttributeCallback callback(Span span) {
        return new OpenTracingAttributeCallback(span);
    }

    private static final class OpenTracingAttributeCallback implements ZeebeForwardingClient.AttributeCallback {

        private final Span span;

        OpenTracingAttributeCallback(Span span) {
            this.span = span;
        }

        @Override
        public ZeebeForwardingClient.AttributeCallback setError() {
            if (span != null) {
                Tags.ERROR.set(span, true);
            }
            return this;
        }

        @Override
        public ZeebeForwardingClient.AttributeCallback setAttribute(String key, String value) {
            if (span != null) {
                this.span.setTag(key, value);
            }
            return this;
        }

        @Override
        public ZeebeForwardingClient.AttributeCallback setAttribute(String key, int value) {
            if (span != null) {
                this.span.setTag(key, value);
            }
            return this;
        }

        @Override
        public ZeebeForwardingClient.AttributeCallback setAttribute(String key, long value) {
            if (span != null) {
                this.span.setTag(key, value);
            }
            return this;
        }

        @Override
        public ZeebeForwardingClient.AttributeCallback setAttribute(String key, boolean value) {
            if (span != null) {
                this.span.setTag(key, value);
            }
            return this;
        }
    }
}
