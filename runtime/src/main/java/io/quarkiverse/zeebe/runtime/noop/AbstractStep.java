package io.quarkiverse.zeebe.runtime.noop;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.FinalCommandStep;
import io.camunda.zeebe.client.api.response.ModifyProcessInstanceResponse;
import io.camunda.zeebe.client.api.response.Topology;
import io.camunda.zeebe.client.impl.response.ModifyProcessInstanceResponseImpl;
import io.camunda.zeebe.client.impl.response.TopologyImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class AbstractStep<T> {

    private static final Map<Class<?>, Object> DATA = new HashMap<>();
    static {
        DATA.put(ModifyProcessInstanceResponse.class,
                new ModifyProcessInstanceResponseImpl(GatewayOuterClass.ModifyProcessInstanceResponse.getDefaultInstance()));
        DATA.put(Topology.class, new TopologyImpl(GatewayOuterClass.TopologyResponse.getDefaultInstance()));
    }

    protected T create() {
        return null;
    }

    protected ZeebeFuture<T> createFuture() {
        return new ZeebeFutureImpl<>(create());
    }

    protected FinalCommandStep<T> createFinal() {
        return new FinalCommandStepImpl<>(create());
    }

    public ZeebeFuture<T> send() {
        return createFuture();
    }

    public FinalCommandStep<T> requestTimeout(Duration requestTimeout) {
        return createFinal();
    }

}
