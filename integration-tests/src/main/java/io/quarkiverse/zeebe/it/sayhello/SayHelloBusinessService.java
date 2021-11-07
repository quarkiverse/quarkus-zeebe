package io.quarkiverse.zeebe.it.sayhello;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SayHelloBusinessService {

    public String hello(String name) {
        return "Hi, " + name;
    }
}
