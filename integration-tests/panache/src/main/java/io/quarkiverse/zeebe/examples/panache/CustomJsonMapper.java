package io.quarkiverse.zeebe.examples.panache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.command.InternalClientException;

@ApplicationScoped
public class CustomJsonMapper implements JsonMapper {

    public static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };

    private static final TypeReference<Map<String, String>> STRING_MAP_TYPE_REFERENCE = new TypeReference<Map<String, String>>() {
    };

    private final ObjectMapper objectMapper;

    public CustomJsonMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public <T> T fromJson(final String json, final Class<T> typeClass) {
        try {
            return objectMapper.readValue(json, typeClass);
        } catch (final IOException e) {
            throw new InternalClientException(
                    String.format("Failed to deserialize json '%s' to class '%s'", json, typeClass), e);
        }
    }

    @Override
    public Map<String, Object> fromJsonAsMap(final String json) {
        try {
            return objectMapper.readValue(json, MAP_TYPE_REFERENCE);
        } catch (final IOException e) {
            throw new InternalClientException(
                    String.format("Failed to deserialize json '%s' to 'Map<String, Object>'", json), e);
        }
    }

    @Override
    public Map<String, String> fromJsonAsStringMap(final String json) {
        try {
            return objectMapper.readValue(json, STRING_MAP_TYPE_REFERENCE);
        } catch (final IOException e) {
            throw new InternalClientException(
                    String.format("Failed to deserialize json '%s' to 'Map<String, String>'", json), e);
        }
    }

    @Override
    public String toJson(final Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            throw new InternalClientException(
                    String.format("Failed to serialize object '%s' to json", value), e);
        }
    }

    @Override
    public String validateJson(final String propertyName, final String jsonInput) {
        try {
            return objectMapper.readTree(jsonInput).toString();
        } catch (final IOException e) {
            throw new InternalClientException(
                    String.format(
                            "Failed to validate json input '%s' for property '%s'", jsonInput, propertyName),
                    e);
        }
    }

    @Override
    public String validateJson(final String propertyName, final InputStream jsonInput) {
        try {
            return objectMapper.readTree(jsonInput).toString();
        } catch (final IOException e) {
            throw new InternalClientException(
                    String.format("Failed to validate json input stream for property '%s'", propertyName), e);
        }
    }
}
