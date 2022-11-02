package io.quarkiverse.zeebe.examples.opentelemetry;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import io.opentelemetry.instrumentation.annotations.WithSpan;

@ApplicationScoped
public class TestService {

    @WithSpan
    public String getParam() {
        return UUID.randomUUID().toString();
    }
}
