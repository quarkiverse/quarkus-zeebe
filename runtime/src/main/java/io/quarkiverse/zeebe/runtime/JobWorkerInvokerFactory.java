package io.quarkiverse.zeebe.runtime;

public interface JobWorkerInvokerFactory {

    default JobWorkerInvoker create(String clazz) {
        return null;
    }

}
