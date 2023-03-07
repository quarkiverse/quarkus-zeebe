package io.quarkiverse.zeebe.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.RecordStreamSource;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.engine.EngineFactory;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.protocol.record.Record;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ZeebeTestEmbeddedResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final Logger log = LoggerFactory.getLogger(ZeebeTestEmbeddedResource.class);

    public static ZeebeTestEngine ZEEBE_ENGINE;

    static ZeebeClient CLIENT;

    @Override
    public Map<String, String> start() {
        int randomPort = findFreeRandomPort();
        log.info("Create Zeebe in-memory engine for test run on random port: " + randomPort + "...");
        ZEEBE_ENGINE = EngineFactory.create(randomPort);
        ZEEBE_ENGINE.start();
        String gatewayAddress = ZEEBE_ENGINE.getGatewayAddress();
        log.info("Zeebe test engine started {}", gatewayAddress);
        CLIENT = ZEEBE_ENGINE.createClient();
        return Map.of("quarkus.zeebe.client.broker.gateway-address", gatewayAddress);
    }

    @Override
    public void stop() {
        ZEEBE_ENGINE.stop();
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(CLIENT,
                new TestInjector.AnnotatedAndMatchesType(InjectZeebeClient.class, ZeebeClient.class));
    }

    private static int findFreeRandomPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Zeebe test engine free random port is not available", e);
        }
    }

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        BpmnAssert.initRecordStream(RecordStream.of(new RecordStreamSourceImpl()));
    }

    public static class RecordStreamSourceImpl implements RecordStreamSource {

        @Override
        public Iterable<Record<?>> getRecords() {
            return ZEEBE_ENGINE.getRecordStreamSource().getRecords();
        }
    }
}
