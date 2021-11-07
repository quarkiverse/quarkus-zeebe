package io.quarkiverse.zeebe;

import java.util.List;

import io.quarkiverse.zeebe.runtime.ZeebeWorkerValue;
import io.quarkus.builder.item.SimpleBuildItem;

public final class ZeebeWorkersBuildItem extends SimpleBuildItem {

    final List<ZeebeWorkerValue> workers;

    public ZeebeWorkersBuildItem() {
        this.workers = null;
    }

    public ZeebeWorkersBuildItem(List<ZeebeWorkerValue> workers) {
        this.workers = workers;
    }

    public List<ZeebeWorkerValue> getWorkers() {
        return workers;
    }
}
