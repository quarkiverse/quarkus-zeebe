package io.quarkiverse.zeebe.runtime.health;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.response.BrokerInfo;
import io.camunda.zeebe.client.api.response.Topology;

@Readiness
public class ZeebeHealthCheck implements HealthCheck {

    @Inject
    ZeebeClient client;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Zeebe health check");
        try {
            Topology topology = client.newTopologyRequest().send().join();
            List<BrokerInfo> brokers = topology.getBrokers();
            if (brokers.isEmpty()) {
                return builder.withData("reason", "No brokers found").down().build();
            }

            return builder
                    .withData("brokers", brokers.stream().map(BrokerInfo::getAddress).collect(Collectors.joining(", ")))
                    .withData("version", topology.getGatewayVersion())
                    .withData("size", topology.getClusterSize())
                    .withData("partitions", topology.getPartitionsCount())
                    .withData("replicationFactor", topology.getReplicationFactor())
                    .up()
                    .build();

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
