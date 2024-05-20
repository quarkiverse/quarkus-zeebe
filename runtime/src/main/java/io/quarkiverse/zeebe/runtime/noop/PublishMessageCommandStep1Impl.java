package io.quarkiverse.zeebe.runtime.noop;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import io.camunda.zeebe.client.impl.response.PublishMessageResponseImpl;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;

public class PublishMessageCommandStep1Impl extends AbstractStep<PublishMessageResponse>
        implements PublishMessageCommandStep1.PublishMessageCommandStep2, PublishMessageCommandStep1.PublishMessageCommandStep3,
        PublishMessageCommandStep1 {

    @Override
    public PublishMessageCommandStep2 messageName(String messageName) {
        return this;
    }

    @Override
    public PublishMessageCommandStep3 correlationKey(String correlationKey) {
        return this;
    }

    @Override
    public PublishMessageCommandStep3 withoutCorrelationKey() {
        return this;
    }

    @Override
    public PublishMessageCommandStep3 messageId(String messageId) {
        return this;
    }

    @Override
    public PublishMessageCommandStep3 timeToLive(Duration timeToLive) {
        return this;
    }

    @Override
    public PublishMessageCommandStep3 variables(InputStream variables) {
        return this;
    }

    @Override
    public PublishMessageCommandStep3 variables(String variables) {
        return this;
    }

    @Override
    public PublishMessageCommandStep3 variables(Map<String, Object> variables) {
        return this;
    }

    @Override
    public PublishMessageCommandStep3 variables(Object variables) {
        return this;
    }

    @Override
    public PublishMessageCommandStep3 variable(String key, Object value) {
        return this;
    }

    @Override
    public PublishMessageCommandStep3 tenantId(String tenantId) {
        return this;
    }

    @Override
    protected PublishMessageResponse create() {
        return new PublishMessageResponseImpl(GatewayOuterClass.PublishMessageResponse.getDefaultInstance());
    }

}
