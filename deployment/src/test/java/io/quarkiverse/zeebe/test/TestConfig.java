package io.quarkiverse.zeebe.test;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.Topology;

@ApplicationScoped
public class TestConfig {

    @Inject
    ZeebeClient client;

    public Topology topology() {
        return client.newTopologyRequest().send().join();
    }
}
