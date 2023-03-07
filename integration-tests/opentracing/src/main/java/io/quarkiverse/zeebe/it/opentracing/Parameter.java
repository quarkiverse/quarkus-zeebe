package io.quarkiverse.zeebe.it.opentracing;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Parameter {

    public String name;

    public String message;

    @Override
    public String toString() {
        return "Parameter{" +
                "name='" + name + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
