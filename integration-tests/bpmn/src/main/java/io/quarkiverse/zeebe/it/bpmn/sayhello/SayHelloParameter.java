package io.quarkiverse.zeebe.it.bpmn.sayhello;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SayHelloParameter {

    public String name;

    public String message;

}
