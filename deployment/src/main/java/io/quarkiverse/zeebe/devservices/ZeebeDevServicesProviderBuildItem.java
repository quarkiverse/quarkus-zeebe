package io.quarkiverse.zeebe.devservices;

import io.quarkus.builder.item.SimpleBuildItem;

public final class ZeebeDevServicesProviderBuildItem extends SimpleBuildItem {

    public ZeebeDevServiceProcessor.ZeebeDevServicesStartResult result;

    public ZeebeDevServicesProviderBuildItem(ZeebeDevServiceProcessor.ZeebeDevServicesStartResult result) {
        this.result = result;
    }

}
