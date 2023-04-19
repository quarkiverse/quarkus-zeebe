package io.quarkiverse.zeebe;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.zeebe.test.CustomJsonMapperProducer;
import io.quarkiverse.zeebe.test.Param;
import io.quarkiverse.zeebe.test.TestService;
import io.quarkus.test.QuarkusUnitTest;

public class CustomJsonMapperTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource("no-resources.properties", "application.properties")
                    .addClasses(CustomJsonMapperProducer.class, TestService.class, Param.class));

    @Inject
    TestService service;

    @Test
    @DisplayName("Test custom JSON mapper")
    public void testLiquibaseNotAvailableWithoutDataSource() {
        Param p = new Param();
        p.name = "test";
        p.value = 100;
        String tmp = service.toJson(p);
        Assertions.assertEquals("{\"name\":\"test\",\"value\":100}", tmp);
    }

}
