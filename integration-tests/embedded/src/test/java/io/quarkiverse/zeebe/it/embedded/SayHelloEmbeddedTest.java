package io.quarkiverse.zeebe.it.embedded;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.quarkiverse.zeebe.test.InjectZeebeClient;
import io.quarkiverse.zeebe.test.ZeebeTestEmbeddedResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@DisplayName("Say Hello Test")
@QuarkusTestResource(ZeebeTestEmbeddedResource.class)
public class SayHelloEmbeddedTest {

    @InjectZeebeClient
    ZeebeClient client;

    @Test
    @DisplayName("Start process")
    public void sayHelloTest() {

        SayHelloParameter p = new SayHelloParameter();
        p.message = "message-test-example";
        p.name = "name-test-input";

        ProcessInstanceEvent processInstance = client
                .newCreateInstanceCommand()
                .bpmnProcessId("hello_process")
                .latestVersion()
                .variables(p)
                .send().join();

        ProcessInstanceAssert assertThat = assertThat(processInstance);
        await().atMost(7, SECONDS).untilAsserted(assertThat::isCompleted);
        assertThat.hasVariableWithValue("name", "name-test-input");
        assertThat.hasVariableWithValue("message", "Hi, name-test-input");

    }

    @Test
    @DisplayName("Start process 2")
    public void sayHelloTest2() {

        SayHelloParameter p = new SayHelloParameter();
        p.message = "message";
        p.name = "Quarkus";

        ProcessInstanceEvent processInstance = client
                .newCreateInstanceCommand()
                .bpmnProcessId("hello_process")
                .latestVersion()
                .variables(p)
                .send().join();

        ProcessInstanceAssert assertThat = assertThat(processInstance);
        await().atMost(7, SECONDS).untilAsserted(assertThat::isCompleted);
        assertThat.hasVariableWithValue("name", "Quarkus");
        assertThat.hasVariableWithValue("message", "Hi, Quarkus");

    }
}
