package io.quarkiverse.zeebe.devservices.monitor;

import io.quarkiverse.zeebe.runtime.devmode.MonitorHandler;
import io.quarkiverse.zeebe.runtime.devmode.MonitorJsonRPCService;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.MenuPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.vertx.ext.web.handler.BodyHandler;

public class MonitorProcessor {

    private static final String ROOT_PATH = "zeebe";
    private static final String SUB_PATH_RECORDS = "records";

    @BuildStep(onlyIf = IsDevelopment.class)
    void myNestedExtensionRoute(BuildProducer<RouteBuildItem> routes,
            BuildProducer<CardPageBuildItem> cardsProducer,
            BuildProducer<MenuPageBuildItem> menuProducer,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) {

        routes.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .nestedRoute(ROOT_PATH, SUB_PATH_RECORDS)
                .handler(BodyHandler.create())
                .build());

        routes.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .nestedRoute(ROOT_PATH, SUB_PATH_RECORDS)
                .handler(new MonitorHandler())
                .build());

        CardPageBuildItem cardPageBuildItem = new CardPageBuildItem();

        cardPageBuildItem.addPage(Page.webComponentPageBuilder()
                .title("Instances")
                .icon("font-awesome-solid:cubes")
                .componentLink("qwc-zeebe-instances.js"));
        cardsProducer.produce(cardPageBuildItem);

        var t = Page.webComponentPageBuilder()
                .title("Processes")
                .icon("font-awesome-solid:cubes")
                .componentLink("qwc-zeebe-processes.js");
        cardPageBuildItem.addPage(t);
        cardsProducer.produce(cardPageBuildItem);

        MenuPageBuildItem menuPageBuildItem = new MenuPageBuildItem();

        menuPageBuildItem.addPage(t);
        menuProducer.produce(menuPageBuildItem);
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    JsonRPCProvidersBuildItem createJsonRPCService() {
        return new JsonRPCProvidersBuildItem(MonitorJsonRPCService.class);
    }
}
