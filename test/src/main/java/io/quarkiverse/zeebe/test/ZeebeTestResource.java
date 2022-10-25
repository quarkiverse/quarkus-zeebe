package io.quarkiverse.zeebe.test;

import java.util.Map;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.quarkiverse.zeebe.runtime.ZeebeClientBuilderFactory;
import io.quarkiverse.zeebe.runtime.ZeebeRuntimeConfig;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ZeebeTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    static ZeebeClient CLIENT;

    @Override
    public Map<String, String> start() {
        return null;
    }

    @Override
    public void stop() {

    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(CLIENT,
                new TestInjector.AnnotatedAndMatchesType(InjectZeebeClient.class, ZeebeClient.class));
    }

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        String gateway = context.devServicesProperties().get("quarkiverse.zeebe.devservices.test.gateway-address");
        if (gateway != null) {
            ZeebeRuntimeConfig config = new ZeebeRuntimeConfig();
            config.broker.gatewayAddress = gateway;
            ZeebeClientBuilder builder = ZeebeClientBuilderFactory.createBuilder(config);
            CLIENT = builder.build();
        }

    }
}
