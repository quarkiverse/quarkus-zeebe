package io.quarkiverse.zeebe.examples.opentracing;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
public class TestService {

    @Traced
    public String getParam() {
        return UUID.randomUUID().toString();
    }
}
