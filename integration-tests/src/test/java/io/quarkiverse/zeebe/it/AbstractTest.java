package io.quarkiverse.zeebe.it;

import io.quarkiverse.zeebe.test.ZeebeTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.RestAssured;
import io.restassured.filter.log.ResponseLoggingFilter;

@QuarkusTestResource(ZeebeTestResource.class)
public class AbstractTest {

    //Configure the containers for the test
    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.filters(new ResponseLoggingFilter());
    }

}
