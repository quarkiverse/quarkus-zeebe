package io.quarkiverse.zeebe.devservices;

import io.quarkus.builder.item.SimpleBuildItem;

public final class ZeebeDevServicesProviderBuildItem extends SimpleBuildItem {

    public String zeebeInternalUrl;

    public ZeebeDevServicesProviderBuildItem(String zeebeInternalUrl) {
        this.zeebeInternalUrl = zeebeInternalUrl;
    }

}
