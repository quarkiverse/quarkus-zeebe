package io.quarkiverse.zeebe.examples.opentelemetry.test1;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import io.opentelemetry.extension.annotations.WithSpan;

@ApplicationScoped
public class Step1Service {

    @WithSpan
    public String getParam() {
        return UUID.randomUUID().toString();
    }
}
