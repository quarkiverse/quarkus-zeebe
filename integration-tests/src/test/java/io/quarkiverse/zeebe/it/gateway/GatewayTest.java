package io.quarkiverse.zeebe.it.gateway;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkiverse.zeebe.it.AbstractTest;
import io.quarkiverse.zeebe.it.ProcessInstanceEventImpl;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@DisplayName("Gateway Test")
public class GatewayTest extends AbstractTest {

    private final static String BPM_PROCESS_ID = "gateway";

    @Test
    @DisplayName("Start gateway process without parameters")
    public void gatewayWithoutParameterTest() {

        Input p = new Input();
        p.read = false;

        ProcessInstanceEvent event = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(p)
                .pathParam("bpmProcessId", BPM_PROCESS_ID)
                .post("/zeebe/{bpmProcessId}")
                .then()
                .statusCode(HttpResponseStatus.OK.code())
                .extract().as(ProcessInstanceEventImpl.class);

        Assertions.assertEquals(BPM_PROCESS_ID, event.getBpmnProcessId());

        Parameter result = wait(event.getProcessInstanceKey(), Parameter.class);
        Assertions.assertNull(result.data);
        Assertions.assertEquals("empty data", result.info);
    }

    @Test
    @DisplayName("Start gateway process with parameters")
    public void gatewayWithParameterTest() {

        Input p = new Input();
        p.read = true;

        ProcessInstanceEvent event = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(p)
                .pathParam("bpmProcessId", BPM_PROCESS_ID)
                .post("/zeebe/{bpmProcessId}")
                .then()
                .statusCode(HttpResponseStatus.OK.code())
                .extract().as(ProcessInstanceEventImpl.class);

        Assertions.assertEquals(BPM_PROCESS_ID, event.getBpmnProcessId());

        Parameter result = wait(event.getProcessInstanceKey(), Parameter.class);
        Assertions.assertEquals("update[read data]", result.data);
        Assertions.assertEquals("update data", result.info);
    }
}
