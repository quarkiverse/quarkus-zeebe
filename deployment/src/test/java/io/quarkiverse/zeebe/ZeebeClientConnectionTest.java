package io.quarkiverse.zeebe;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.api.response.Topology;
import io.quarkiverse.zeebe.jobworker.Parameter;
import io.quarkiverse.zeebe.jobworker.TestJobWorker;
import io.quarkiverse.zeebe.test.TestConfig;
import io.quarkus.test.QuarkusUnitTest;

public class ZeebeClientConnectionTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource("default.properties", "application.properties")
                    .addClasses(TestConfig.class, Parameter.class, TestJobWorker.class));

    @Inject
    TestConfig config;

    @Test
    @DisplayName("Test default connection")
    public void testLiquibaseNotAvailableWithoutDataSource() {
        Assertions.assertNotNull(config);
        Topology topology = config.topology();
        Assertions.assertNotNull(topology);
    }

}
