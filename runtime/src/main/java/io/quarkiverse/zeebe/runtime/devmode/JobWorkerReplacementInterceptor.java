package io.quarkiverse.zeebe.runtime.devmode;

import io.grpc.*;
import io.quarkiverse.zeebe.ZeebeClientInterceptor;

public class JobWorkerReplacementInterceptor implements ZeebeClientInterceptor {

    private static volatile Runnable onMessage;

    public static void onMessage(Runnable handler) {
        onMessage = handler;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
            Channel next) {
        onMessage.run();
        return next.newCall(method, callOptions);
    }
}
