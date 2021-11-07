package io.quarkiverse.zeebe;

import java.util.Collection;

import io.quarkus.builder.item.SimpleBuildItem;

public final class ZeebeResourcesBuildItem extends SimpleBuildItem {

    final Collection<String> resources;

    public ZeebeResourcesBuildItem() {
        this.resources = null;
    }

    public ZeebeResourcesBuildItem(Collection<String> resources) {
        this.resources = resources;
    }

    public Collection<String> getResources() {
        return resources;
    }
}
