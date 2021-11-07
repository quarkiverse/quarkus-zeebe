package io.quarkiverse.zeebe.it;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.awaitility.Awaitility.await;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.RestAssured;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;

@QuarkusTestResource(ZeebeResource.class)
public class AbstractTest {

    //Configure the containers for the test
    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.filters(new ResponseLoggingFilter());
    }

    public static <T> T wait(Long processInstanceKey, Class<T> clazz) {
        WaitResult<T> wait = new WaitResult<>();
        await()
                .atMost(7, SECONDS)
                .untilAsserted(() -> {
                    Response response = given()
                            .when()
                            .contentType(APPLICATION_JSON)
                            .get("/result/" + processInstanceKey);
                    response.then().statusCode(HttpResponseStatus.OK.code());
                    wait.param = response.as(clazz);
                });
        return wait.param;
    }

    public static final class WaitResult<T> {
        T param;
    }
}
