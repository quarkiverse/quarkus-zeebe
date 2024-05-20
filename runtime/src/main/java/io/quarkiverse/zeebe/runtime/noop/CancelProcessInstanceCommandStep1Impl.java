package io.quarkiverse.zeebe.runtime.noop;

import io.camunda.zeebe.client.api.command.CancelProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.response.CancelProcessInstanceResponse;
import io.camunda.zeebe.client.impl.response.CancelProcessInstanceResponseImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class CancelProcessInstanceCommandStep1Impl extends AbstractStep<CancelProcessInstanceResponse>
        implements CancelProcessInstanceCommandStep1 {

    @Override
    protected CancelProcessInstanceResponse create() {
        return new CancelProcessInstanceResponseImpl(GatewayOuterClass.CancelProcessInstanceResponse.getDefaultInstance());
    }
}
