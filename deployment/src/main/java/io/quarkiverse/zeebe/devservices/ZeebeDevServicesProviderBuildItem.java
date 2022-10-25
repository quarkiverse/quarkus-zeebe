package io.quarkiverse.zeebe.devservices;

import io.quarkus.builder.item.SimpleBuildItem;

public final class ZeebeDevServicesProviderBuildItem extends SimpleBuildItem {

    public String internalUrl;

    public ZeebeDevServicesProviderBuildItem(String internalUrl) {
        this.internalUrl = internalUrl;
    }

}
