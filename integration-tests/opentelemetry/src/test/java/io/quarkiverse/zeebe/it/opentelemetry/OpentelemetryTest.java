package io.quarkiverse.zeebe.it.opentelemetry;

import static io.quarkiverse.zeebe.it.opentelemetry.TestResources.JAEGER_HOST;
import static io.quarkiverse.zeebe.it.opentelemetry.TestResources.JAEGER_PORT;
import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.quarkiverse.zeebe.test.InjectZeebeClient;
import io.quarkiverse.zeebe.test.ZeebeTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

@QuarkusTest
@DisplayName("Open-telemetry test")
@QuarkusTestResource(ZeebeTestResource.class)
@QuarkusTestResource(TestResources.class)
public class OpentelemetryTest {

    //Configure the containers for the test
    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        //        RestAssured.filters(new ResponseLoggingFilter());
    }

    private final static String BPM_PROCESS_ID = "test";

    @InjectZeebeClient
    ZeebeClient client;

    @Test
    @DisplayName("Test open telemetry")
    public void testOpenTelemetry() throws JsonProcessingException {

        Parameter param = new Parameter();
        param.message = "message-example";
        param.name = "name-input";

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(BPM_PROCESS_ID)
                .latestVersion()
                .variables(param)
                .send().join();

        Assertions.assertEquals(BPM_PROCESS_ID, event.getBpmnProcessId());
        ProcessInstanceAssert a = BpmnAssert.assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);

        await().atMost(7, SECONDS).until(() -> jaegerTrace().getList("data").size() > 0);

        JsonPath response = jaegerTrace();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JaegerResponse jr = objectMapper.readValue(response.prettify(), JaegerResponse.class);
        Assertions.assertNotNull(jr);
        Assertions.assertNotNull(jr.data);

        Map<String, JaegerSpan> spans = new HashMap<>();
        jr.data.forEach(x -> {
            x.spans.forEach(y -> {
                spans.put(y.operationName, y);
            });
        });

        JaegerSpan deploymentSpan = spans.get("DeployResourceRequest");
        Assertions.assertNotNull(deploymentSpan);
        Map<String, JaegerTag> deploymentTags = deploymentSpan.tags.stream()
                .collect(Collectors.toMap(x -> x.key, x -> x));
        assertTag(deploymentTags, "otel.library.name", "io.quarkus.opentelemetry");
        Assertions.assertTrue(deploymentTags.get("bpmn-deploy-resources").value.endsWith("test.bpmn"));

        JaegerSpan testMethod = spans.get("openTelemetryTestMethod");
        Assertions.assertNotNull(testMethod);
        Map<String, JaegerTag> testTags = testMethod.tags.stream()
                .collect(Collectors.toMap(x -> x.key, x -> x));
        assertTag(testTags, "otel.library.name", "io.quarkus.opentelemetry");
        assertTag(testTags, "bpmn-component", "job-worker");
        assertTag(testTags, "bpmn-process-id", "test");
        assertTag(testTags, "bpmn-process-element-id", "Activity_0apsury");
        assertTag(testTags, "bpmn-process-def-ver", "1");
        assertTag(testTags, "bpmn-retries", "3");
        assertTag(testTags, "bpmn-class", "io.quarkiverse.zeebe.it.opentelemetry.OpentelemetryTestJobWorker");
        assertTag(testTags, "bpmn-job-variables", "{\"name\":\"name-input\",\"message\":\"message-example\"}");
        assertTag(testTags, "bpmn-job-type", "test");

        JaegerSpan completeSpan = spans.get("CompleteJobRequest");
        Assertions.assertNotNull(completeSpan);
        Map<String, JaegerTag> completeTags = completeSpan.tags.stream()
                .collect(Collectors.toMap(x -> x.key, x -> x));
        assertTag(completeTags, "otel.library.name", "io.quarkus.opentelemetry");
        assertTag(completeTags, "bpmn-job-variables", "{\"name\":\"name-input\",\"message\":\"Ok\"}");

    }

    private void assertTag(Map<String, JaegerTag> tags, String name, Object value) {
        Assertions.assertNotNull(tags.get(name));
        Assertions.assertEquals(value, tags.get(name).value);
    }

    private JsonPath jaegerTrace() {
        return given().baseUri(JAEGER_HOST).port(JAEGER_PORT)
                .when()
                .get("/api/traces?service=quarkus-zeebe-integration-tests-opentelemetry&limit=40&lookback=1h")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .jsonPath();
    }

    public static class JaegerResponse {
        public List<JaegerTrace> data;
    }

    public static class JaegerTrace {
        public List<JaegerSpan> spans;
    }

    public static class JaegerSpan {
        public String operationName;

        public List<JaegerTag> tags;
    }

    public static class JaegerTag {

        public String key;
        public String value;
    }
}
