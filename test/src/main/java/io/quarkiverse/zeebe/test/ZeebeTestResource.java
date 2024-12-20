package io.quarkiverse.zeebe.test;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.impl.ZeebeClientBuilderImpl;
import io.camunda.zeebe.process.test.api.RecordStreamSource;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.protocol.record.Record;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.zeebe.containers.exporter.DebugReceiver;

public class ZeebeTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final Logger log = LoggerFactory.getLogger(ZeebeTestResource.class);

    static ZeebeClient CLIENT;

    static final List<Record<?>> RECORDS = new CopyOnWriteArrayList<>();

    static DebugReceiver RECEIVER;

    @Override
    public Map<String, String> start() {
        return null;
    }

    @Override
    public void stop() {
        if (RECEIVER != null) {
            RECEIVER.stop();
        }
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(CLIENT,
                new TestInjector.AnnotatedAndMatchesType(InjectZeebeClient.class, ZeebeClient.class));
    }

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        String gateway = context.devServicesProperties().get("quarkiverse.zeebe.devservices.test.gateway-address");
        String restAddress = context.devServicesProperties().get("quarkiverse.zeebe.devservices.test.rest-address");
        if (gateway != null || restAddress != null) {
            CLIENT = createClient(gateway, restAddress);
        }
        String receiverPort = context.devServicesProperties().get("quarkiverse.zeebe.devservices.test.receiver-port");
        if (receiverPort != null) {
            int port = Integer.parseInt(receiverPort);
            RECEIVER = new DebugReceiver(RECORDS::add, port, true);
            RECEIVER.start();
            log.info("Debug receiver started at http://localhost:{}", port);
            BpmnAssert.initRecordStream(RecordStream.of(new RecordStreamSourceImpl()));
        }
    }

    public static class RecordStreamSourceImpl implements RecordStreamSource {

        @Override
        public Iterable<Record<?>> getRecords() {
            return RECORDS;
        }
    }

    private static ZeebeClient createClient(String gateway, String restAddress) {
        return new ZeebeClientBuilderImpl().gatewayAddress(gateway).usePlaintext()
                .restAddress(URI.create(restAddress)).build();
    }
}
