package io.quarkiverse.zeebe.test;

import java.util.Map;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.quarkiverse.zeebe.runtime.ZeebeClientBuilderFactory;
import io.quarkiverse.zeebe.runtime.ZeebeRuntimeConfig;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ZeebeTestResource implements QuarkusTestResourceLifecycleManager {

    static RecordStreamSourceImpl RECORDS;

    static ZeebeClient CLIENT;

    @Override
    public Map<String, String> start() {

        String gateway = System.getProperty("quarkus.zeebe.devservices.test.gateway-address");
        if (gateway != null) {
            ZeebeRuntimeConfig config = new ZeebeRuntimeConfig();
            config.broker.gatewayAddress = gateway;
            ZeebeClientBuilder builder = ZeebeClientBuilderFactory.createBuilder(config);
            CLIENT = builder.build();
        }

        String address = System.getProperty("quarkus.zeebe.devservices.test.hazelcast");
        if (address != null) {
            RECORDS = new RecordStreamSourceImpl(address);
            BpmnAssert.init(RECORDS);
        }
        return null;
    }

    @Override
    public void stop() {
        try {
            if (RECORDS != null) {
                RECORDS.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(CLIENT,
                new TestInjector.AnnotatedAndMatchesType(InjectZeebeClient.class, ZeebeClient.class));
    }

}
