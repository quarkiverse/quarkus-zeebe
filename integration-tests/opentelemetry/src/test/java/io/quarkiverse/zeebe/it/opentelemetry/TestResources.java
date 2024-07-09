package io.quarkiverse.zeebe.it.opentelemetry;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class TestResources implements QuarkusTestResourceLifecycleManager {

    private static final Logger log = LoggerFactory.getLogger(TestResources.class);

    public static String JAEGER_HOST;
    public static Integer JAEGER_PORT;

    GenericContainer<?> jaeger = new GenericContainer<>("jaegertracing/all-in-one:1.58")
            .waitingFor(new BoundPortHttpWaitStrategy(16686))
            .withEnv("COLLECTOR_OTLP_ENABLED", "true")
            .withExposedPorts(16686, 4317);

    @Override
    public Map<String, String> start() {
        jaeger.start();

        JAEGER_HOST = String.format("http://%s", jaeger.getHost());
        JAEGER_PORT = jaeger.getMappedPort(16686);
        log.info("JAEGER_HOST: {}, JAEGER_PORT: {}", JAEGER_HOST, JAEGER_PORT);
        return Map.of("quarkus.otel.exporter.otlp.traces.endpoint",
                String.format("http://%s:%s", jaeger.getHost(), jaeger.getMappedPort(4317)));
    }

    @Override
    public void stop() {
        jaeger.stop();
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
