package io.quarkiverse.zeebe.it.bpmn.sayhello;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    }

}
