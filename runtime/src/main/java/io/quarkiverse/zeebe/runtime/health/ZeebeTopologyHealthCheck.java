package io.quarkiverse.zeebe.runtime.health;

import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.response.Topology;

@Liveness
public class ZeebeTopologyHealthCheck implements HealthCheck {

    @Inject
    ZeebeClient client;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Zeebe topology health check");
        try {
            Topology topology = client.newTopologyRequest().send().join();
            if (topology.getClusterSize() > 0) {
                return builder.up().build();
            }
            return builder.down().withData("reason", "No brokers found").build();
        } catch (ClientStatusException e) {
            return builder.down()
                    .withData("reason", e.getMessage())
                    .withData("code", e.getStatusCode().value())
                    .build();
        } catch (Exception ex) {
            return builder.down().withData("reason", ex.getMessage()).build();
        }
    }
}
