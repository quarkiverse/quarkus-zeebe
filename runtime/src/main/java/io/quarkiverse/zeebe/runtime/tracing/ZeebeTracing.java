package io.quarkiverse.zeebe.runtime.tracing;

public class ZeebeTracing {

    static String COMPONENT_NAME = "zeebe-worker";

    static String WORKER_EXCEPTION = "bpmn-worker-exception";

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

    static String getName(Class<?> c) {
        return c.getName().replace("_Subclass", "");
    }
}
