package io.quarkiverse.zeebe.it.opentelemetry;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class TestResources implements QuarkusTestResourceLifecycleManager {

    public static final String JAEGER_IMAGE = "jaegertracing/all-in-one:latest";
    public static final String OTL_IMAGE = "otel/opentelemetry-collector:0.48.0";
    public static final int JAEGER_ADMIN_PORT = 14269;
    public static String JAEGER_HOST;
    public static Integer JAEGER_PORT;

    GenericContainer<?> jaeger = new GenericContainer<>(JAEGER_IMAGE).withNetworkAliases("jaeger-all-in-one")
            .withNetwork(Network.SHARED)
            .waitingFor(new BoundPortHttpWaitStrategy(JAEGER_ADMIN_PORT))
            .withExposedPorts(16686, 14269);

    GenericContainer<?> otl = new GenericContainer<>(OTL_IMAGE)
            .withNetworkAliases("otel-collector")
            .withNetwork(Network.SHARED)
            .withClasspathResourceMapping("/otel-collector-config.yaml", "/etc/otelcol/config.yaml", BindMode.READ_ONLY)
            .waitingFor(new BoundPortHttpWaitStrategy(13133))
            .withExposedPorts(4317, 55680, 13133);

    @Override
    public Map<String, String> start() {
        jaeger.start();
        otl.start();
        JAEGER_HOST = getBaseUrl(jaeger);
        JAEGER_PORT = jaeger.getMappedPort(16686);
        return Map.of("quarkus.opentelemetry.tracer.exporter.otlp.endpoint", getUrl(otl, 4317));
    }

    @Override
    public void stop() {
        otl.stop();
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
