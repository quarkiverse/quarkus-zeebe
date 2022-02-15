package io.quarkiverse.zeebe;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.api.response.Topology;
import io.quarkiverse.zeebe.test.TestConfig;
import io.quarkus.test.QuarkusUnitTest;

public class ZeebeHazelcastConnectionTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource("hazelcast.properties", "application.properties")
                    .addClass(TestConfig.class));

    @Inject
    TestConfig config;

    @Test
    @DisplayName("Test hazelcast connection")
    public void testLiquibaseNotAvailableWithoutDataSource() {
        Assertions.assertNotNull(config);
        Topology topology = config.topology();
        Assertions.assertNotNull(topology);
    }

}
