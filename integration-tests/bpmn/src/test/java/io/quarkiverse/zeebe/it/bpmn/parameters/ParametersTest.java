package io.quarkiverse.zeebe.it.bpmn.parameters;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.quarkiverse.zeebe.it.bpmn.AbstractTest;
import io.quarkiverse.zeebe.test.InjectZeebeClient;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@DisplayName("Parameters Test")
public class ParametersTest extends AbstractTest {

    private static final String BPM_PROCESS_ID = "ParametersProcessTest";

    @InjectZeebeClient
    ZeebeClient client;

    @Test
    @DisplayName("Start process")
    public void sayHelloTest() {

        Parameter p = new Parameter();
        p.info = "info-input";
        p.data = "data-input";

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(BPM_PROCESS_ID)
                .latestVersion()
                .variables(p)
                .send().join();

        Assertions.assertEquals(BPM_PROCESS_ID, event.getBpmnProcessId());

        ProcessInstanceAssert a = BpmnAssert.assertThat(event);
        await().atMost(15, SECONDS).untilAsserted(a::isCompleted);

    }

}
