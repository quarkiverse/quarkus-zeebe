package io.quarkiverse.zeebe.runtime.noop;

import java.io.InputStream;
import java.util.Map;

import io.camunda.zeebe.client.api.command.ThrowErrorCommandStep1;

public class ThrowErrorCommandStep1Impl extends AbstractStep<Void>
        implements ThrowErrorCommandStep1, ThrowErrorCommandStep1.ThrowErrorCommandStep2 {

    @Override
    public ThrowErrorCommandStep2 errorCode(String errorCode) {
        return this;
    }

    @Override
    public ThrowErrorCommandStep2 errorMessage(String errorMsg) {
        return this;
    }

    @Override
    public ThrowErrorCommandStep2 variables(InputStream variables) {
        return this;
    }

    @Override
    public ThrowErrorCommandStep2 variables(String variables) {
        return this;
    }

    @Override
    public ThrowErrorCommandStep2 variables(Map<String, Object> variables) {
        return this;
    }

    @Override
    public ThrowErrorCommandStep2 variables(Object variables) {
        return this;
    }

    @Override
    public ThrowErrorCommandStep2 variable(String key, Object value) {
        return this;
    }

}
