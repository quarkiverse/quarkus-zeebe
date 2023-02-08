package io.quarkiverse.zeebe;

import io.quarkus.arc.Unremovable;

@Unremovable
public interface JobWorkerExceptionHandler {

    void handleError(JobWorkerCommand command, Throwable throwable);
}
