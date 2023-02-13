package io.quarkiverse.zeebe.it.microprofile.metrics;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MetricsTestParameter {

    public String name;

    public String message;

}
