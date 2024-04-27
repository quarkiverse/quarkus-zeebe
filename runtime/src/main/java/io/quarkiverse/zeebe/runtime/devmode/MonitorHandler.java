package io.quarkiverse.zeebe.devservices.monitor;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.protocol.jackson.ZeebeProtocolModule;
import io.camunda.zeebe.protocol.record.Record;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class MonitorHandler implements Handler<RoutingContext> {

    private static final String SERVER_HEADER = "Server";

    private static final String SERVER_INFO = "zpt-debug/1.1";

    private static final Logger log = LoggerFactory.getLogger(MonitorHandler.class);

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new ZeebeProtocolModule());

    @Override
    public void handle(RoutingContext rc) {

        var response = rc.response();
        var bytes = rc.body().buffer().getBytes();

        final List<Record<?>> records;
        try {
            records = MAPPER.readValue(bytes, new TypeReference<List<Record<?>>>() {
            });

            if (records == null || records.isEmpty()) {
                response(response, 204);
                return;
            }

            for (final Record<?> record : records) {
                log.info("Add record {}}/{} ==> {}", record.getValueType(), record.getPosition(), record.getRecordType());
                log.info("{}", record);
            }

            response(response, 200);
        } catch (final IOException e) {
            log.warn("Failed to deserialize exported records", e);
            response(response, 400);
        }
    }

    private void response(HttpServerResponse response, int statusCode) {
        response.setStatusCode(statusCode).putHeader(SERVER_HEADER, SERVER_INFO)
                .end();
    }
}
