package io.quarkiverse.zeebe.it.microprofile.metrics;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.quarkiverse.zeebe.test.InjectZeebeClient;
import io.quarkiverse.zeebe.test.ZeebeTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@DisplayName("Microprofile metrics test")
@QuarkusTestResource(ZeebeTestResource.class)
public class MetricsTest {

    //Configure the containers for the test
    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private static final String PREFIX = "application_camunda_job_invocations_total";
    private static final int LENGTH = PREFIX.length();

    private final static String BPM_PROCESS_ID = "metrics_test";

    private final static String BPM_PROCESS_TYPE_1 = "metrics_test";

    private final static String COMPLETED = "completed";

    private final static String ACTIVATED = "activated";

    @InjectZeebeClient
    ZeebeClient client;

    @Test
    @DisplayName("Check microprofile metrics values")
    public void readyHealthCheckTest() {

        MetricsTestParameter p = new MetricsTestParameter();
        p.message = "message-example";
        p.name = "name-input";

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(BPM_PROCESS_ID)
                .latestVersion()
                .variables(p)
                .send().join();

        Assertions.assertEquals(BPM_PROCESS_ID, event.getBpmnProcessId());
        ProcessInstanceAssert a = BpmnAssert.assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);

        String response = given()
                .when()
                .get("/q/metrics")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().asString();

        final Map<String, Map<String, Double>> metrics = new HashMap<>();

        response.lines().filter(x -> x != null && x.startsWith(PREFIX))
                .map(x -> x.substring(LENGTH).split(" "))
                .forEach(x -> map(metrics, x));

        Assertions.assertNotNull(metrics.get(BPM_PROCESS_TYPE_1));
        Assertions.assertNotNull(metrics.get(BPM_PROCESS_TYPE_1).get(COMPLETED));
        Assertions.assertEquals(metrics.get(BPM_PROCESS_TYPE_1).get(COMPLETED), 1.0);
        Assertions.assertNotNull(metrics.get(BPM_PROCESS_TYPE_1).get(ACTIVATED));
        Assertions.assertEquals(metrics.get(BPM_PROCESS_TYPE_1).get(ACTIVATED), 1.0);

        event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(BPM_PROCESS_ID)
                .latestVersion()
                .variables(p)
                .send().join();

        Assertions.assertEquals(BPM_PROCESS_ID, event.getBpmnProcessId());
        a = BpmnAssert.assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);

        event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(BPM_PROCESS_ID)
                .latestVersion()
                .variables(p)
                .send().join();

        Assertions.assertEquals(BPM_PROCESS_ID, event.getBpmnProcessId());
        a = BpmnAssert.assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);

        response = given()
                .when()
                .get("/q/metrics")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().asString();

        metrics.clear();
        response.lines().filter(x -> x != null && x.startsWith(PREFIX))
                .map(x -> x.substring(LENGTH).split(" "))
                .forEach(x -> map(metrics, x));

        Assertions.assertNotNull(metrics.get(BPM_PROCESS_TYPE_1));
        Assertions.assertNotNull(metrics.get(BPM_PROCESS_TYPE_1).get(COMPLETED));
        Assertions.assertEquals(metrics.get(BPM_PROCESS_TYPE_1).get(COMPLETED), 3.0);
        Assertions.assertNotNull(metrics.get(BPM_PROCESS_TYPE_1).get(ACTIVATED));
        Assertions.assertEquals(metrics.get(BPM_PROCESS_TYPE_1).get(ACTIVATED), 3.0);
    }

    private void map(Map<String, Map<String, Double>> metrics, String[] data) {
        String[] tmp2 = data;
        String tmp = tmp2[0];
        Double count = Double.parseDouble(tmp2[1]);
        tmp = tmp.substring(1, tmp.length() - 1);
        tmp2 = tmp.split(",");
        String action = trimPrefix(tmp2[0]);
        String type = trimPrefix(tmp2[1]);
        metrics.computeIfAbsent(type, s -> new HashMap<>()).put(action, count);
    }

    private String trimPrefix(String data) {
        return data.substring(data.indexOf("\"") + 1, data.length() - 1);
    }
}
