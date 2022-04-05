package io.quarkiverse.zeebe.examples.opentracing.test1;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.opentracing.Traced;

@Traced
@ApplicationScoped
public class Step1Service {

    public String getParam() {
        return UUID.randomUUID().toString();
    }
}
