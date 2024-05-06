package io.quarkiverse.zeebe.runtime.devmode;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.protocol.jackson.ZeebeProtocolModule;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.quarkiverse.zeebe.runtime.devmode.store.RecordStore;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class ZeebeRecordsHandler implements Handler<RoutingContext> {

    private static final String SERVER_HEADER = "Server";

    private static final String SERVER_INFO = "zpt-debug/1.1";

    private static final Logger log = LoggerFactory.getLogger(ZeebeRecordsHandler.class);

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new ZeebeProtocolModule());

    private static final Map<Integer, Long> positions = new HashMap<>();

    @Override
    public void handle(RoutingContext rc) {
        var buffer = rc.body().buffer();
        process(rc.response(), buffer);
    }

    private void process(HttpServerResponse response, Buffer buffer) {
        if (buffer.length() == 0) {
            response(response, 400);
        }

        var bytes = buffer.getBytes();
        final List<Record<?>> records;
        try {
            records = MAPPER.readValue(bytes, new TypeReference<List<Record<?>>>() {
            });

            if (records == null || records.isEmpty()) {
                response(response, 204);
                return;
            }

            for (final Record<?> record : records) {
                //                log.info("Add record {}}/{} ==> {}", record.getValueType(), record.getPosition(), record.getRecordType());
                if (record.getRecordType() == RecordType.EVENT) {
                    switch (record.getValueType()) {
                        case PROCESS_INSTANCE -> RecordStore.importProcessInstance(value(record));
                        case PROCESS -> RecordStore.importProcess(value(record));
                        case TIMER -> RecordStore.importTimer(value(record));
                        case PROCESS_MESSAGE_SUBSCRIPTION -> RecordStore.importMessageSubscription(value(record));
                        case MESSAGE_START_EVENT_SUBSCRIPTION ->
                            RecordStore.importMessageStartEventSubscription(value(record));
                        case ERROR -> RecordStore.importError(value(record));
                        case INCIDENT -> RecordStore.importIncident(value(record));
                        case JOB -> RecordStore.importJob(value(record));
                        case MESSAGE -> RecordStore.importMessage(value(record));
                        case VARIABLE -> RecordStore.importVariable(value(record));
                        case SIGNAL -> RecordStore.importSignal(value(record));
                        case SIGNAL_SUBSCRIPTION -> RecordStore.importSignalSubscription(value(record));
                        case ESCALATION -> RecordStore.importEscalation(value(record));
                    }
                }

                positions.merge(record.getPartitionId(), record.getPosition(), Math::max);
            }

            createSuccessfulResponse(response, records.get(0).getPartitionId());

        } catch (final IOException e) {
            log.warn("Failed to deserialize exported records", e);
            response(response, 400);
        }
    }

    private static <T> T value(Record<?> record) {
        @SuppressWarnings("unchecked")
        T result = (T) record;
        return result;
    }

    private void createSuccessfulResponse(HttpServerResponse response, int partitionId) {
        final Long position = positions.get(partitionId);
        if (position == null) {
            response(response, 204);
            return;
        }

        try {
            var responseBody = MAPPER.writeValueAsString(Collections.singletonMap("position", position));
            response.setStatusCode(200).putHeader(SERVER_HEADER, SERVER_INFO)
                    .end(responseBody);
        } catch (Exception ex) {
            log.warn("Failed to serialize response", ex);
            response(response, 400);
        }
    }

    private void response(HttpServerResponse response, int statusCode) {
        response.setStatusCode(statusCode).putHeader(SERVER_HEADER, SERVER_INFO)
                .end();
    }
}
