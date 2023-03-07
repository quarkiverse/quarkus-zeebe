package io.quarkiverse.zeebe;

import java.util.List;

import io.quarkiverse.zeebe.runtime.JobWorkerMetadata;
import io.quarkus.builder.item.SimpleBuildItem;

public final class ZeebeWorkersBuildItem extends SimpleBuildItem {

    final List<JobWorkerMetadata> workers;

    public ZeebeWorkersBuildItem() {
        this.workers = null;
    }

    public ZeebeWorkersBuildItem(List<JobWorkerMetadata> workers) {
        this.workers = workers;
    }

    public List<JobWorkerMetadata> getWorkers() {
        return workers;
    }

}
