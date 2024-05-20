package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.command.TopologyRequestStep1;
import io.camunda.zeebe.client.api.response.Topology;
import io.camunda.zeebe.client.impl.response.TopologyImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class TopologyRequestStep1Impl extends AbstractStep<Topology> implements TopologyRequestStep1 {

    @Override
    public TopologyRequestStep1 useRest() {
        return this;
    }

    @Override
    public TopologyRequestStep1 useGrpc() {
        return this;
    }

    @Override
    protected Topology create() {
        return new TopologyImpl(GatewayOuterClass.TopologyResponse.getDefaultInstance());
    }
}
