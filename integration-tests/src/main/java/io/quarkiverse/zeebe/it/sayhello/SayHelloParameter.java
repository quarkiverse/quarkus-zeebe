package io.quarkiverse.zeebe.it.sayhello;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SayHelloParameter {

    public String name;

    public String message;

}
