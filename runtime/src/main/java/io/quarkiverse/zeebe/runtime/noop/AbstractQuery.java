package io.quarkiverse.zeebe.runtime.noop;

import java.time.Duration;
import java.util.function.Consumer;

import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.search.SearchRequestPage;
import io.camunda.zeebe.client.api.search.query.FinalSearchQueryStep;
import io.camunda.zeebe.client.api.search.response.SearchQueryResponse;

public class AbstractQuery<T, Q, F, S> {

    public FinalSearchQueryStep<T> requestTimeout(Duration requestTimeout) {
        return null;
    }

    public ZeebeFuture<SearchQueryResponse<T>> send() {
        return null;
    }

    public Q filter(F value) {
        return (Q) this;
    }

    public Q filter(Consumer<F> fn) {
        return (Q) this;
    }

    public Q sort(S value) {
        return (Q) this;
    }

    public Q sort(Consumer<S> fn) {
        return (Q) this;
    }

    public Q page(SearchRequestPage value) {
        return (Q) this;
    }

    public Q page(Consumer<SearchRequestPage> fn) {
        return (Q) this;
    }
}
