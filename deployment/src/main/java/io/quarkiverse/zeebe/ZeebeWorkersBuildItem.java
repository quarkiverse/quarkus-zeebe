package io.quarkiverse.zeebe;

import java.util.List;

import io.quarkiverse.zeebe.runtime.JobWorkerMetadata;
import io.quarkus.builder.item.SimpleBuildItem;

public final class ZeebeWorkersBuildItem extends SimpleBuildItem {

    final List<JobWorkerMetadata> workers;

    final String factory;

    public ZeebeWorkersBuildItem() {
        this.workers = null;
        this.factory = null;
    }

    public ZeebeWorkersBuildItem(List<JobWorkerMetadata> workers, String factory) {
        this.workers = workers;
        this.factory = factory;
    }

    public List<JobWorkerMetadata> getWorkers() {
        return workers;
    }

    public String getFactory() {
        return factory;
    }
}
