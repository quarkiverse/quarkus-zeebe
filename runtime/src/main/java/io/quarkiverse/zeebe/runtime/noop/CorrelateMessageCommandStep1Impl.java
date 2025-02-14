package io.quarkiverse.zeebe.runtime.noop;

import java.io.InputStream;
import java.util.Map;

import io.camunda.zeebe.client.api.command.CorrelateMessageCommandStep1;
import io.camunda.zeebe.client.api.response.CorrelateMessageResponse;

public class CorrelateMessageCommandStep1Impl extends AbstractStep<CorrelateMessageResponse>
        implements CorrelateMessageCommandStep1, CorrelateMessageCommandStep1.CorrelateMessageCommandStep2,
        CorrelateMessageCommandStep1.CorrelateMessageCommandStep3 {

    @Override
    public CorrelateMessageCommandStep2 messageName(String messageName) {
        return this;
    }

    @Override
    public CorrelateMessageCommandStep3 correlationKey(String correlationKey) {
        return this;
    }

    @Override
    public CorrelateMessageCommandStep3 withoutCorrelationKey() {
        return this;
    }

    @Override
    public CorrelateMessageCommandStep3 variables(InputStream variables) {
        return this;
    }

    @Override
    public CorrelateMessageCommandStep3 variables(String variables) {
        return this;
    }

    @Override
    public CorrelateMessageCommandStep3 variables(Map<String, Object> variables) {
        return this;
    }

    @Override
    public CorrelateMessageCommandStep3 variables(Object variables) {
        return this;
    }

    @Override
    public CorrelateMessageCommandStep3 variable(String key, Object value) {
        return this;
    }

    @Override
    public CorrelateMessageCommandStep3 tenantId(String tenantId) {
        return this;
    }
}
