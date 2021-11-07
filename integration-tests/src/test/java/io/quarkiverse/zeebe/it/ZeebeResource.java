package io.quarkiverse.zeebe.it;

import java.util.Collections;
import java.util.Map;

import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.zeebe.containers.ZeebeContainer;
import io.zeebe.containers.ZeebeDefaults;

public class ZeebeResource implements QuarkusTestResourceLifecycleManager {

    public static String ZEEBE_VERSION = "1.2.2";

    public ZeebeContainer zeebe = new ZeebeContainer(
            DockerImageName.parse(ZeebeDefaults.getInstance().getDefaultImage()).withTag(ZEEBE_VERSION));

    @Override
    public Map<String, String> start() {
        zeebe.start();
        return Collections.singletonMap(
                "quarkus.zeebe.client.broker.gateway-address", zeebe.getExternalAddress(26500));
    }

    @Override
    public void stop() {
        zeebe.stop();
    }
}
