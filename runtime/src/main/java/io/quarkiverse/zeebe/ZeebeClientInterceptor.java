package io.quarkiverse.zeebe;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;

/**
 * Zeebe client interceptor qualifier for class which implements <b>ZeebeClientInterceptor</b> interface.
 *
 * @see io.grpc.ClientInterceptor
 */
public interface ZeebeClientInterceptor {

    <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next);
}
