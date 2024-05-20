package io.quarkiverse.zeebe.runtime.noop;

import java.io.InputStream;
import java.util.Map;

import io.camunda.zeebe.client.api.command.EvaluateDecisionCommandStep1;
import io.camunda.zeebe.client.api.response.EvaluateDecisionResponse;
import io.camunda.zeebe.client.impl.ZeebeObjectMapper;
import io.camunda.zeebe.client.impl.response.EvaluateDecisionResponseImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class EvaluateDecisionCommandStep1Impl extends AbstractStep<EvaluateDecisionResponse>
        implements EvaluateDecisionCommandStep1, EvaluateDecisionCommandStep1.EvaluateDecisionCommandStep2 {
    @Override
    public EvaluateDecisionCommandStep2 decisionId(String decisionId) {
        return this;
    }

    @Override
    public EvaluateDecisionCommandStep2 decisionKey(long decisionKey) {
        return this;
    }

    @Override
    public EvaluateDecisionCommandStep2 variables(InputStream variables) {
        return this;
    }

    @Override
    public EvaluateDecisionCommandStep2 variables(String variables) {
        return this;
    }

    @Override
    public EvaluateDecisionCommandStep2 variables(Map<String, Object> variables) {
        return this;
    }

    @Override
    public EvaluateDecisionCommandStep2 variables(Object variables) {
        return this;
    }

    @Override
    public EvaluateDecisionCommandStep2 variable(String key, Object value) {
        return this;
    }

    @Override
    public EvaluateDecisionCommandStep2 tenantId(String tenantId) {
        return this;
    }

    @Override
    protected EvaluateDecisionResponse create() {
        return new EvaluateDecisionResponseImpl(new ZeebeObjectMapper(),
                GatewayOuterClass.EvaluateDecisionResponse.getDefaultInstance());
    }
}
