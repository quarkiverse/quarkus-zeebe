package io.quarkiverse.zeebe.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.Topology;

@ApplicationScoped
public class TestService {

    @Inject
    ZeebeClient client;

    @Inject
    JsonMapper jsonMapper;

    public Topology topology() {
        return client.newTopologyRequest().send().join();
    }

    public ProcessInstanceEvent startProcess(String name, Object parameter) {
        return client.newCreateInstanceCommand()
                .bpmnProcessId(name)
                .latestVersion()
                .variables(parameter)
                .send().join();

    }

    public String toJson(Object value) {
        return jsonMapper.toJson(value);
    }
}
