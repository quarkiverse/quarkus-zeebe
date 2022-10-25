package io.quarkiverse.zeebe.it.docker.sayhello;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.quarkiverse.zeebe.it.docker.AbstractTest;
import io.quarkiverse.zeebe.test.InjectZeebeClient;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@DisplayName("Say Hello Test")
public class SayHelloTest extends AbstractTest {

    @InjectZeebeClient
    ZeebeClient client;

    @Test
    @DisplayName("Start process")
    public void sayHelloTest() {

        SayHelloParameter p = new SayHelloParameter();
        p.message = "message-example";
        p.name = "name-input";

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId("hello_process")
                .latestVersion()
                .variables(p)
                .send().join();

        ProcessInstanceAssert a = BpmnAssert.assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);
        a.hasVariableWithValue("name", "name-input");
        a.hasVariableWithValue("message", "Hello name-input");
    }

}
