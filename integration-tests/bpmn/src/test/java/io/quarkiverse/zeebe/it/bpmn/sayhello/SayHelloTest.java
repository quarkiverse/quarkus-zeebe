package io.quarkiverse.zeebe.it.bpmn.sayhello;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.quarkiverse.zeebe.it.bpmn.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@DisplayName("Say Hello Test")
public class SayHelloTest extends AbstractTest {

    @Test
    @DisplayName("Start process")
    public void sayHelloTest() {

        SayHelloParameter p = new SayHelloParameter();
        p.message = "message-test-example";
        p.name = "name-test-input";

        long processInstanceKey = given().contentType(ContentType.JSON)
                .body(p).when()
                .post("/say-hello")
                .then().log().body().extract()
                .jsonPath().getLong("processInstanceKey");

        ProcessInstanceAssert a = new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream());
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);
        a.hasVariableWithValue("name", "name-test-input");
        a.hasVariableWithValue("message", "Hi, name-test-input");
    }

}
