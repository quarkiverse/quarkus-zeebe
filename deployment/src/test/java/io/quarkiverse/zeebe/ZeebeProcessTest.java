package io.quarkiverse.zeebe;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.quarkiverse.zeebe.jobworker.Parameter;
import io.quarkiverse.zeebe.jobworker.TestJobWorker;
import io.quarkiverse.zeebe.test.TestService;
import io.quarkus.test.QuarkusUnitTest;

public class ZeebeProcessTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource("default.properties", "application.properties")
                    .addAsResource("bpmn/TestProcess.bpmn", "/bpmn/TestProcess.bpmn")
                    .addClasses(TestService.class, Parameter.class, TestJobWorker.class));

    @Inject
    TestService service;

    @Test
    @DisplayName("Test process")
    public void testLiquibaseNotAvailableWithoutDataSource() {
        Parameter param = new Parameter();
        param.info = "info";
        param.data = "data";
        ProcessInstanceEvent tmp = service.startProcess("TestProcess", param);
        Assertions.assertNotNull(tmp);
    }

}
