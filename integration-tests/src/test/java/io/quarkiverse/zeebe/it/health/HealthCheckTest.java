package io.quarkiverse.zeebe.it.health;

import static io.restassured.RestAssured.given;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkiverse.zeebe.it.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;

@QuarkusTest
@DisplayName("Health check test")
public class HealthCheckTest extends AbstractTest {

    @Test
    @DisplayName("Ready health check")
    public void readyHealthCheckTest() {
        JsonPath response = given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .get("/q/health/ready")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().jsonPath();

        Assertions.assertEquals("UP", response.get("status"));
        List<Object> checks = response.getList("checks");
        Assertions.assertNotNull(checks);
        Assertions.assertEquals(1, checks.size());
    }

    @Test
    @DisplayName("Live health check")
    public void liveHealthCheckTest() {
        JsonPath response = given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .get("/q/health/live")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().jsonPath();

        Assertions.assertEquals("UP", response.get("status"));
        List<Object> checks = response.getList("checks");
        Assertions.assertNotNull(checks);
        Assertions.assertEquals(1, checks.size());
    }
}
