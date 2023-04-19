package io.quarkiverse.zeebe.it.bpmn.parameters;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkiverse.zeebe.*;
import io.quarkus.logging.Log;

@ApplicationScoped
public class ParameterJobWorker {

    @Inject
    ObjectMapper mapper;

    @JobWorker(type = "test-default")
    public void testDefault(ActivatedJob job, JobClient client) {
        Log.infof("Default parameters 'test-default' parameters: %s", job.getVariablesAsMap());
    }

    @JobWorker(type = "test-variable-headers")
    public void testVariableHeaders(@Variable String info, @CustomHeaders Map<String, String> headers) {
        Log.infof("Variable parameters 'info' value: %s, headers: %s", info, headers);
    }

    @JobWorker(type = "test-variable-header")
    public void testVariableHeader(@Variable String info, @CustomHeader String header1) {
        Log.infof("Variable parameters 'info' value: %s, header 'header1' value: %s", info, header1);
    }

    @JobWorker(type = "test-variable-header-name")
    public void testVariableHeaderName(@Variable String info, @CustomHeader("header2") String header) {
        Log.infof("Variable parameters 'info' value: %s, header 'header2' value: %s", info, header);
    }

    @JobWorker(type = "test-variable")
    public void testVariable(@Variable String info) {
        Log.infof("Variable parameters 'info' value: %s", info);
    }

    @JobWorker(type = "test-variable-name")
    public void testVariableName(@Variable("info") String tmp) {
        Log.infof("Variable parameters 'info' value: %s", tmp);
    }

    @JobWorker(type = "test-variable-as")
    public void testVariableAs(@VariablesAsType Parameter param) {
        Log.infof("Variable parameters value: %s", param);
    }

    @JobWorker(type = "test-variable-return-object")
    public Parameter testVariableAsReturn(@VariablesAsType Parameter param) {
        Log.infof("Variable parameters value: %s", param);
        return param;
    }

    @JobWorker(type = "test-variable-return-map")
    public Map<String, Object> testVariableAsReturnMap(@VariablesAsType Parameter param) {
        Map<String, Object> tmp = mapper.convertValue(param, new TypeReference<Map<String, Object>>() {
        });
        Log.infof("Variable parameters value: %s return map %s", param, tmp);
        return tmp;
    }

    @JobWorker(type = "test-variable-return-string")
    public String testVariableAsReturnString(@VariablesAsType Parameter param) {
        String tmp;
        try {
            tmp = mapper.writeValueAsString(param);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Log.infof("Test variableAs value: %s return string %s", param, tmp);
        return tmp;
    }

    @JobWorker(type = "test-variable-return-input-stream")
    public InputStream testVariableAsReturnInputStream(@VariablesAsType Parameter param) {
        byte[] tmp;
        try {
            tmp = mapper.writeValueAsBytes(param);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Log.infof("Test variableAs value: %s return input-stream", param);
        return new ByteArrayInputStream(tmp);
    }
}
