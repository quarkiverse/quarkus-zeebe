package io.quarkiverse.zeebe.examples.panache;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.quarkiverse.zeebe.test.ZeebeTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
@DisplayName("Person Test")
@QuarkusTestResource(ZeebeTestResource.class)
public class PersonTest {

    //Configure the containers for the test
    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        //        RestAssured.filters(new ResponseLoggingFilter());
    }

    @Test
    public void createPersonTest() {
        PersonRestController.CreateRequestDTO dto = new PersonRestController.CreateRequestDTO();
        dto.name = "Test";
        dto.birth = "2001-08-16";

        long processInstanceKey = given().contentType(ContentType.JSON)
                .body(dto).when()
                .post("/person")
                .then()
                .extract().jsonPath().getLong("processInstanceKey");

        ProcessInstanceAssert a = new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream());
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);

        Person response = given().contentType(ContentType.JSON)
                .pathParam("name", dto.name).when()
                .get("/person/{name}/name")
                .then().extract().as(Person.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(dto.name, response.name);
        Assertions.assertEquals(LocalDate.parse(dto.birth), response.birth);
        Assertions.assertNotNull(response.age);
    }

    @Test
    public void createNoNamePersonTest() {
        PersonRestController.CreateRequestDTO dto = new PersonRestController.CreateRequestDTO();
        dto.name = "NoName";
        dto.birth = "2001-08-16";

        long processInstanceKey = given().contentType(ContentType.JSON)
                .body(dto).when()
                .post("/person")
                .then()
                .extract().jsonPath().getLong("processInstanceKey");

        ProcessInstanceAssert a = new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream());
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);

        Person response = given().contentType(ContentType.JSON)
                .pathParam("name", dto.name).when()
                .get("/person/{name}/name")
                .then().extract().as(Person.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(dto.name, response.name);
        Assertions.assertEquals(LocalDate.parse(dto.birth), response.birth);
        Assertions.assertNull(response.age);
    }
}
