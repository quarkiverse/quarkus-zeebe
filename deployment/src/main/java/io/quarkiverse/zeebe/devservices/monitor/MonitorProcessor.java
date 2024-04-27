package io.quarkiverse.zeebe.devservices.monitor;

import io.quarkiverse.zeebe.runtime.devmode.MonitorHandler;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.vertx.ext.web.handler.BodyHandler;

public class MonitorProcessor {

    private static final String ROOT_PATH = "zeebe";
    private static final String SUB_PATH_RECORDS = "records";

    @BuildStep
    void myNestedExtensionRoute(BuildProducer<RouteBuildItem> routes,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) {
        routes.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .nestedRoute(ROOT_PATH, SUB_PATH_RECORDS)
                .handler(BodyHandler.create())
                .build());

        routes.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .nestedRoute(ROOT_PATH, SUB_PATH_RECORDS)
                .handler(new MonitorHandler())
                .build());
    }
}
