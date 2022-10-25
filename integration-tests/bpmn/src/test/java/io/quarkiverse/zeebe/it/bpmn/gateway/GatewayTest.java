package io.quarkiverse.zeebe.it.bpmn.gateway;

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
@DisplayName("Gateway Test")
public class GatewayTest extends AbstractTest {

    @InjectZeebeClient
    ZeebeClient client;

    private final static String BPM_PROCESS_ID = "gateway";

    @Test
    @DisplayName("Start gateway process without parameters")
    public void gatewayWithoutParameterTest() {

        Input p = new Input();
        p.read = false;

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(BPM_PROCESS_ID)
                .latestVersion()
                .variables(p)
                .send().join();

        Assertions.assertEquals(BPM_PROCESS_ID, event.getBpmnProcessId());

        ProcessInstanceAssert a = BpmnAssert.assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);
        a.hasVariableWithValue("read", false);
        a.hasVariableWithValue("info", "empty data");
    }

    @Test
    @DisplayName("Start gateway process with parameters")
    public void gatewayWithParameterTest() {

        Input p = new Input();
        p.read = true;

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(BPM_PROCESS_ID)
                .latestVersion()
                .variables(p)
                .send().join();

        Assertions.assertEquals(BPM_PROCESS_ID, event.getBpmnProcessId());

        ProcessInstanceAssert a = BpmnAssert.assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);
        a.hasVariableWithValue("read", true);
        a.hasVariableWithValue("info", "update data");
        a.hasVariableWithValue("data", "update[read data]");
    }
}
