package io.quarkiverse.zeebe.devservices;

import io.quarkus.builder.item.SimpleBuildItem;

public final class ZeebeDevServicesProviderBuildItem extends SimpleBuildItem {

    public ZeebeDevServicesStartResult result;

    public ZeebeDevServicesProviderBuildItem(ZeebeDevServicesStartResult result) {
        this.result = result;
    }

}
