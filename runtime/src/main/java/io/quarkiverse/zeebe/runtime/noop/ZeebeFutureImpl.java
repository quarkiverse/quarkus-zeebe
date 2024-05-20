package io.quarkiverse.zeebe.runtime.noop;

import java.util.concurrent.TimeUnit;

import io.camunda.zeebe.client.impl.http.HttpZeebeFuture;

public class ZeebeFutureImpl<T> extends HttpZeebeFuture<T> {

    private final T response;

    public ZeebeFutureImpl(T response) {
        this.response = response;
    }

    @Override
    public T join() {
        return response;
    }

    @Override
    public T join(long timeout, TimeUnit unit) {
        return response;
    }

}
