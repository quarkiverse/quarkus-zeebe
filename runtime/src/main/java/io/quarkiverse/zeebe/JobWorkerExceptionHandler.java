package io.quarkiverse.zeebe;

import io.quarkiverse.zeebe.runtime.JobWorkerCommand;
import io.quarkus.arc.Unremovable;

@Unremovable
public interface JobWorkerExceptionHandler {

    void handleError(JobWorkerCommand command, Throwable throwable);

    class WarningException extends RuntimeException {
        public WarningException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
