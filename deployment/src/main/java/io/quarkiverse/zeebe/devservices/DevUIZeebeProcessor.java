package io.quarkiverse.zeebe.devservices;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.builditem.RuntimeConfigSetupCompleteBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;

public class DevUIZeebeProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    @Consume(RuntimeConfigSetupCompleteBuildItem.class)
    public CardPageBuildItem pages(NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) {

        CardPageBuildItem cardPageBuildItem = new CardPageBuildItem();

        return cardPageBuildItem;
    }

}
