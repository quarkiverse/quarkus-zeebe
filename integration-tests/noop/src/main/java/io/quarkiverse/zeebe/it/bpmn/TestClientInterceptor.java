package io.quarkiverse.zeebe.it.bpmn;

import jakarta.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.quarkiverse.zeebe.ZeebeClientInterceptor;

@ApplicationScoped
public class TestClientInterceptor implements ZeebeClientInterceptor {

    static Logger log = LoggerFactory.getLogger(TestClientInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
            Channel next) {
        log.info("Client call: {}", method.getFullMethodName());
        return next.newCall(method, callOptions);
    }
}
