package io.quarkiverse.zeebe;

public interface JobWorkerExceptionHandler {

    void handleError(JobWorkerCommand command, Throwable throwable);
}
