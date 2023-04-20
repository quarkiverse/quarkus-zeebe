package io.quarkiverse.zeebe.it.bpmn.sayhello;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SayHelloBusinessService {

    public String hello(String name) {
        return "Hi, " + name;
    }
}
