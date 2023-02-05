package io.quarkiverse.zeebe.runtime;

import static java.util.Map.entry;

import java.util.Map;

import javax.inject.Singleton;

import org.jboss.logging.Logger;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkiverse.zeebe.JobWorkerCommand;
import io.quarkiverse.zeebe.JobWorkerExceptionHandler;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@Singleton
@Unremovable
@DefaultBean
public class DefaultJobWorkerExceptionHandler implements JobWorkerExceptionHandler {

    private static final Logger LOG = Logger.getLogger(JobWorkerHandler.class);

    public enum ACTION {
        SUCCESS,
        RETRIEVABLE,
        IGNORABLE,
        FAILURE;
    }

    private static final Map<Status.Code, ACTION> CODES = Map.ofEntries(
            // SUCCESS
            entry(Status.Code.OK, ACTION.SUCCESS),
            entry(Status.Code.ALREADY_EXISTS, ACTION.SUCCESS),
            // RETRIEVABLE
            entry(Status.Code.CANCELLED, ACTION.RETRIEVABLE),
            entry(Status.Code.DEADLINE_EXCEEDED, ACTION.RETRIEVABLE),
            entry(Status.Code.RESOURCE_EXHAUSTED, ACTION.RETRIEVABLE),
            entry(Status.Code.ABORTED, ACTION.RETRIEVABLE),
            entry(Status.Code.UNAVAILABLE, ACTION.RETRIEVABLE),
            entry(Status.Code.DATA_LOSS, ACTION.RETRIEVABLE),
            // IGNORABLE
            entry(Status.Code.NOT_FOUND, ACTION.IGNORABLE),
            // FAILURE
            entry(Status.Code.INVALID_ARGUMENT, ACTION.FAILURE),
            entry(Status.Code.PERMISSION_DENIED, ACTION.FAILURE),
            entry(Status.Code.FAILED_PRECONDITION, ACTION.FAILURE),
            entry(Status.Code.OUT_OF_RANGE, ACTION.FAILURE),
            entry(Status.Code.UNIMPLEMENTED, ACTION.FAILURE),
            entry(Status.Code.INTERNAL, ACTION.FAILURE),
            entry(Status.Code.UNAUTHENTICATED, ACTION.FAILURE));

    @Override
    public void handleError(JobWorkerCommand command, Throwable throwable) {
        if (!StatusRuntimeException.class.isAssignableFrom(throwable.getClass())) {
            throw new RuntimeException("Could not execute " + command + " due to exception: " + throwable.getMessage(),
                    throwable);
        }

        StatusRuntimeException exception = (StatusRuntimeException) throwable;
        Status.Code code = exception.getStatus().getCode();

        switch (CODES.get(code)) {
            case FAILURE:
                throw new RuntimeException("Could not execute " + command + " due to error of type '" + code + "'", throwable);
            case RETRIEVABLE:
                if (command.canRetry()) {
                    command.supplyRetryDelay();
                    command.retry(Infrastructure.getDefaultWorkerPool());
                    LOG.warn("Retry " + command + " after error of type '" + code + "' with backoff");
                    return;
                }
                throw new RuntimeException(
                        "Could not execute " + command + " due to error of type '" + code + "' and no retries are left",
                        throwable);
            case IGNORABLE:
                LOG.warnf("Ignoring the error of type '%s' during %s. Job might have been canceled or already completed.", code,
                        command);
        }

    }
}
