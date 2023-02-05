package io.quarkiverse.zeebe.runtime.tracing;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.quarkiverse.zeebe.JobWorker;

public class ZeebeTracing {

    static String REQUEST_TIMEOUT = "bpmn-request-timeout";

    static String DEPLOY_RESOURCES = "bpmn-deploy-resources";

    static String COMPONENT_NAME = "job-worker";

    static String CLIENT_EXCEPTION = "bpmn-client-exception";

    static String JOB_EXCEPTION = "bpmn-job-exception";

    static String JOB_TYPE = "bpmn-job-type";

    static String JOB_KEY = "bpmn-job-key";

    static String JOB_RETRIES = "bpmn-job-retries";

    static String JOB_VARIABLES = "bpmn-job-variables";

    static String PROCESS_VARIABLES = "bpmn-process-variables";

    static String PROCESS_VARIABLES_SCOPE = "bpmn-process-variables-scope";

    static String INCIDENT_KEY = "bpmn-incident-key";

    static String MESSAGE_CORRELATION_KEY = "bpmn-message-correlation-key";

    static String MESSAGE_ID = "bpmn-message-id";

    static String MESSAGE_NAME = "bpmn-message-name";

    static String MESSAGE_VARIABLES = "bpmn-message-variables";

    static String MESSAGE_TIME_TO_LIVE = "bpmn-message-time-to-live";

    static String CLASS = "bpmn-class";

    static String COMPONENT = "bpmn-component";

    static String PROCESS_ID = "bpmn-process-id";

    static String PROCESS_INSTANCE_KEY = "bpmn-process-instance-key";

    static String PROCESS_ELEMENT_ID = "bpmn-process-element-id";

    static String PROCESS_ELEMENT_INSTANCE_KEY = "bpmn-process-element-instance-key";

    static String PROCESS_DEF_KEY = "bpmn-process-def-key";

    static String PROCESS_DEF_VER = "bpmn-process-def-ver";

    static String RETRIES = "bpmn-retries";

    static String FAIL_MESSAGE = "bpmn-fail-message";

    static String THROW_ERROR_MESSAGE = "bpmn-throw-error-message";

    static String THROW_ERROR_CODE = "bpmn-throw-error-code";

    static String getClass(Class<?> c) {
        return c.getName().replace("_Subclass", "");
    }

    static Set<String> ATTRIBUTES = new HashSet<>(List.of(
            PROCESS_ID, PROCESS_INSTANCE_KEY, PROCESS_ELEMENT_ID, PROCESS_ELEMENT_INSTANCE_KEY,
            PROCESS_DEF_KEY, PROCESS_DEF_VER, RETRIES, COMPONENT, JOB_TYPE, JOB_KEY, JOB_VARIABLES, CLASS));

    public static void setAttributes(List<String> attributes) {
        ATTRIBUTES = new HashSet<>(attributes);
    }

    static String getSpanName(ActivatedJob job, Method method) {
        JobWorker zw = method.getDeclaredAnnotation(JobWorker.class);
        if (zw != null && zw.name() != null && !zw.name().isBlank()) {
            return zw.name();
        }
        return job.getType();
    }

    static void setAttributes(String clazz, ActivatedJob job, AttributeConfigCallback call) {
        call.setAttributeCheck(CLASS, clazz);
        call.setAttributeCheck(JOB_TYPE, job.getType());
        call.setAttributeCheck(COMPONENT, COMPONENT_NAME);
        call.setAttributeCheck(JOB_KEY, job.getKey());
        call.setAttributeCheck(JOB_VARIABLES, job.getVariables());
        call.setAttributeCheck(PROCESS_ID, job.getBpmnProcessId());
        call.setAttributeCheck(PROCESS_INSTANCE_KEY, job.getProcessInstanceKey());
        call.setAttributeCheck(PROCESS_ELEMENT_ID, job.getElementId());
        call.setAttributeCheck(PROCESS_ELEMENT_INSTANCE_KEY, job.getElementInstanceKey());
        call.setAttributeCheck(PROCESS_DEF_KEY, job.getProcessDefinitionKey());
        call.setAttributeCheck(PROCESS_DEF_VER, job.getProcessDefinitionVersion());
        call.setAttributeCheck(RETRIES, job.getRetries());
    }

    public abstract static class AttributeConfigCallback {

        void setAttributeCheck(String key, long value) {
            if (ATTRIBUTES.contains(key)) {
                setAttribute(key, value);
            }
        }

        void setAttributeCheck(String key, String value) {
            if (ATTRIBUTES.contains(key)) {
                setAttribute(key, value);
            }
        }

        abstract void setAttribute(String key, long value);

        abstract void setAttribute(String key, String value);
    }

}
