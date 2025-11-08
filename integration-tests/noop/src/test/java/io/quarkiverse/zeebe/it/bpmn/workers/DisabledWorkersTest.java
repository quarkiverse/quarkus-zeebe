package io.quarkiverse.zeebe.it.bpmn.workers;

import static io.restassured.RestAssured.given;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkiverse.zeebe.it.bpmn.AbstractTest;
import io.quarkiverse.zeebe.it.bpmn.sayhello.SayHelloParameter;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

@QuarkusTest
@DisplayName("Disabled Workers Test")
@TestProfile(DisabledWorkersTest.DisabledWorkersProfile.class)
public class DisabledWorkersTest extends AbstractTest {

    public static class DisabledWorkersProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Collections.singletonMap("quarkus.zeebe.client.workers.disabled", "true");
        }

    }

    @Test
    @DisplayName("Start process")
    public void sayHelloTest() {

        SayHelloParameter p = new SayHelloParameter();
        p.message = "message-test-example";
        p.name = "name-test-input";

        ExtractableResponse<Response> extract = given().contentType(ContentType.JSON)
                .body(p).when()
                .post("/say-hello")
                .then().log().body().extract();

        String workerVariable = extract
                .jsonPath().getString("variablesAsMap.name");

        Assertions.assertNull(workerVariable);

    }

}
