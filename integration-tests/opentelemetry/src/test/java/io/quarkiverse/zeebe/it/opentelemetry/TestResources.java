package io.quarkiverse.zeebe.it.opentelemetry;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class TestResources implements QuarkusTestResourceLifecycleManager {

    public static String JAEGER_HOST;
    public static Integer JAEGER_PORT;

    GenericContainer<?> jaeger = new GenericContainer<>("jaegertracing/all-in-one:latest")
            .waitingFor(new BoundPortHttpWaitStrategy(16686))
            .withEnv("COLLECTOR_OTLP_ENABLED", "true")
            .withExposedPorts(16686, 4317);

    @Override
    public Map<String, String> start() {
        jaeger.start();

        JAEGER_HOST = getBaseUrl(jaeger);
        JAEGER_PORT = jaeger.getMappedPort(16686);
        return Map.of("quarkus.otel.exporter.otlp.traces.endpoint", getUrl(jaeger, 4317));
    }

    @Override
    public void stop() {
        jaeger.stop();
    }

    private String getUrl(GenericContainer<?> container, int port) {
        return String.format("http://%s:%s", container.getHost(), container.getMappedPort(port));
    }

    private String getBaseUrl(GenericContainer<?> container) {
        return String.format("http://%s", container.getHost());
    }

    public static class BoundPortHttpWaitStrategy extends HttpWaitStrategy {
        private final int port;

        public BoundPortHttpWaitStrategy(int port) {
            this.port = port;
        }

        @Override
        protected Set<Integer> getLivenessCheckPorts() {
            return Collections.singleton(this.waitStrategyTarget.getMappedPort(port));
        }
    }
}
