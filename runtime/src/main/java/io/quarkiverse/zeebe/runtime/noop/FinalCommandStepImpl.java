package io.quarkiverse.zeebe.runtime.noop;

import java.time.Duration;

import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.FinalCommandStep;

public class FinalCommandStepImpl<T> implements FinalCommandStep<T> {

    private final T data;

    FinalCommandStepImpl(T data) {
        this.data = data;
    }

    @Override
    public FinalCommandStep<T> requestTimeout(Duration requestTimeout) {
        return this;
    }

    @Override
    public ZeebeFuture<T> send() {
        return new ZeebeFutureImpl<>(data);
    }
}
