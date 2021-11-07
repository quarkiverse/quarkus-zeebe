package io.quarkiverse.zeebe.it.sayhello;

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
@DisplayName("Say Hello Test")
public class SayHelloTest extends AbstractTest {

    @Test
    @DisplayName("Start process")
    public void sayHelloTest() {
        String bpmProcessId = "hello_process";

        SayHelloParameter p = new SayHelloParameter();
        p.message = "message-example";
        p.name = "name-input";

        ProcessInstanceEvent event = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(p)
                .pathParam("bpmProcessId", bpmProcessId)
                .post("/zeebe/{bpmProcessId}")
                .then()
                .statusCode(HttpResponseStatus.OK.code())
                .extract().as(ProcessInstanceEventImpl.class);

        Assertions.assertEquals(bpmProcessId, event.getBpmnProcessId());

        SayHelloParameter result = wait(event.getProcessInstanceKey(), SayHelloParameter.class);
        Assertions.assertEquals("name-input", result.name);
        Assertions.assertEquals("Hi, name-input", result.message);
    }

}
