package io.quarkiverse.zeebe.runtime.metrics;

public interface MetricsRecorder {

    String METRIC_NAME_JOB = "camunda.job.invocations";
    String ACTION_ACTIVATED = "activated";
    String ACTION_COMPLETED = "completed";
    String ACTION_FAILED = "failed";

    String ACTION_COMPLETED_FAILED = "completed-failed";

    String ACTION_BPMN_ERROR_FAILED = "bpmn-error-failed";

    String ACTION_BPMN_ERROR = "bpmn-error";

    void increase(String name, String action, String type);

    void executeWithTimer(String name, Runnable method);

}
