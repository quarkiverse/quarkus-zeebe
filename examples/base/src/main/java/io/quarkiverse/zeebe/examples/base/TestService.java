package io.quarkiverse.zeebe.examples.base;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestService {

    public String getParam() {
        return UUID.randomUUID().toString();
    }
}
