package io.quarkiverse.zeebe.it.bpmn.errors;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.IncidentAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.protocol.record.value.ErrorType;
import io.quarkiverse.zeebe.it.bpmn.AbstractTest;
import io.quarkiverse.zeebe.test.InjectZeebeClient;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@DisplayName("Errors Test")
public class ErrorsTest extends AbstractTest {

    @InjectZeebeClient
    ZeebeClient client;

    private final static String FAIL_PROCESS_ID = "fail-process";
    private final static String THROW_ERROR_PROCESS_ID = "throw-zeebe-error-process";
    private final static String THROW_ERROR_EVENT_PROCESS_ID = "throw-zeebe-error-event-process";
    private final static String THROW_RUNTIME_EXCEPTION_PROCESS_ID = "throw-runtime-exception-process";

    @Test
    @DisplayName("Start throw zeebe error process")
    public void throwZeebeBpmnErrorTest() {

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(THROW_ERROR_PROCESS_ID)
                .latestVersion()
                .send().join();

        Assertions.assertEquals(THROW_ERROR_PROCESS_ID, event.getBpmnProcessId());

        ProcessInstanceAssert a = assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::hasAnyIncidents);
        IncidentAssert incident = a.extractingLatestIncident();
        incident.hasErrorType(ErrorType.UNHANDLED_ERROR_EVENT);
        incident.extractingErrorMessage().isEqualTo(
                "Expected to throw an error event with the code 'error-code' with message 'error-message', but it was not caught. No error events are available in the scope.");
    }

    @Test
    @DisplayName("Start throw zeebe error event process")
    public void throwZeebeBpmnErrorEventTest() {

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(THROW_ERROR_EVENT_PROCESS_ID)
                .latestVersion()
                .send().join();

        Assertions.assertEquals(THROW_ERROR_EVENT_PROCESS_ID, event.getBpmnProcessId());

        ProcessInstanceAssert a = assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);
    }

    @Test
    @DisplayName("Start throw runtime exception process")
    public void throwRuntimeExceptionTest() {

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(THROW_RUNTIME_EXCEPTION_PROCESS_ID)
                .latestVersion()
                .send().join();

        Assertions.assertEquals(THROW_RUNTIME_EXCEPTION_PROCESS_ID, event.getBpmnProcessId());

        ProcessInstanceAssert a = assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::hasAnyIncidents);
        IncidentAssert incident = a.extractingLatestIncident();
        incident.hasErrorType(ErrorType.JOB_NO_RETRIES);
        incident.extractingErrorMessage()
                .startsWith("java.lang.RuntimeException: error-code");

    }

    @Test
    @DisplayName("Start fail process")
    public void failTest() {

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(FAIL_PROCESS_ID)
                .latestVersion()
                .send().join();

        Assertions.assertEquals(FAIL_PROCESS_ID, event.getBpmnProcessId());

        ProcessInstanceAssert a = assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::hasAnyIncidents);
        IncidentAssert incident = a.extractingLatestIncident();
        incident.hasErrorType(ErrorType.JOB_NO_RETRIES);
        incident.extractingErrorMessage()
                .startsWith("error message");
    }
}
