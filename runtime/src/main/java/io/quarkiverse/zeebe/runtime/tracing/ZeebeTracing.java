package io.quarkiverse.zeebe.runtime.tracing;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.quarkiverse.zeebe.ZeebeWorker;

public class ZeebeTracing {

    static String COMPONENT_NAME = "zeebe-worker";

    static String WORKER_EXCEPTION = "bpmn-worker-exception";

    static String WORKER_TYPE = "bpmn-worker-type";

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
            PROCESS_DEF_KEY, PROCESS_DEF_VER, RETRIES, COMPONENT, WORKER_TYPE, CLASS));

    public static void setAttributes(List<String> attributes) {
        ATTRIBUTES = new HashSet<>(attributes);
    }

    static String getSpanName(ActivatedJob job, Method method) {
        ZeebeWorker zw = method.getDeclaringClass().getDeclaredAnnotation(ZeebeWorker.class);
        if (zw != null && zw.name() != null && !zw.name().isBlank()) {
            return zw.name();
        }
        return job.getType();
    }

    static void setAttributes(String clazz, ActivatedJob job, AttributeCallback call) {
        call.setAttributeCheck(CLASS, clazz);
        call.setAttributeCheck(WORKER_TYPE, job.getType());
        call.setAttributeCheck(COMPONENT, COMPONENT_NAME);
        call.setAttributeCheck(PROCESS_ID, job.getBpmnProcessId());
        call.setAttributeCheck(PROCESS_INSTANCE_KEY, job.getProcessInstanceKey());
        call.setAttributeCheck(PROCESS_ELEMENT_ID, job.getElementId());
        call.setAttributeCheck(PROCESS_ELEMENT_INSTANCE_KEY, job.getElementInstanceKey());
        call.setAttributeCheck(PROCESS_DEF_KEY, job.getProcessDefinitionKey());
        call.setAttributeCheck(PROCESS_DEF_VER, job.getProcessDefinitionVersion());
        call.setAttributeCheck(RETRIES, job.getRetries());
    }

    public abstract static class AttributeCallback {

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
