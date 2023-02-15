package io.quarkiverse.zeebe.it.bpmn.gateway;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Input {

    public boolean read;

    public String message;

    @Override
    public String toString() {
        return "Input{" +
                "read=" + read +
                ", message='" + message + '\'' +
                '}';
    }
}
