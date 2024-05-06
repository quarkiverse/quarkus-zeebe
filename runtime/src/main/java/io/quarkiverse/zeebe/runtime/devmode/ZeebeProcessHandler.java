package io.quarkiverse.zeebe.runtime.devmode;

import java.util.List;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

public class ZeebeProcessHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext rc) {
        if (rc.request().method() == HttpMethod.POST) {
            deployProcess(rc.response(), rc.fileUploads());
        }
    }

    private void deployProcess(HttpServerResponse response, List<FileUpload> files) {
        if (files == null || files.isEmpty()) {
            response.setStatusCode(400).end();
            return;
        }

        files.forEach(f -> {
            System.out.println(f.name());
            System.out.println(f.fileName());
            System.out.println(f.uploadedFileName());
        });

        response.setStatusCode(200).end();
    }
}
