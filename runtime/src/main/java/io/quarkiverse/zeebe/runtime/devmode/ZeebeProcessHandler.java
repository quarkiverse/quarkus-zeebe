package io.quarkiverse.zeebe.runtime.devmode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.ZeebeClient;
import io.quarkiverse.zeebe.runtime.ZeebeClientService;
import io.quarkus.arc.Arc;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

public class ZeebeProcessHandler implements Handler<RoutingContext> {

    private static final Logger log = LoggerFactory.getLogger(ZeebeProcessHandler.class);

    @Override
    public void handle(RoutingContext rc) {
        var cmd = rc.request().getParam("cmd");
        switch (cmd) {
            case "process-deploy":
                if (rc.request().method() == HttpMethod.POST) {
                    deployProcess(rc.response(), rc.fileUploads());
                }
                break;
            case "process-start":
                break;
        }
    }

    private void deployProcess(HttpServerResponse response, List<FileUpload> files) {
        if (files == null || files.isEmpty()) {
            response.setStatusCode(400).end();
            return;
        }

        ZeebeClientService clientService = Arc.container().instance(ZeebeClientService.class).get();
        ZeebeClient client = clientService.client();

        try {
            for (FileUpload file : files) {
                log.info("Zeebe Dev UI start deployment of process '{}'", file.fileName());
                client.newDeployResourceCommand()
                        .addResourceBytes(Files.readAllBytes(Path.of(file.uploadedFileName())), file.fileName())
                        .send().join();
                log.info("Zeebe Dev UI process deployed '{}'", file.fileName());
            }
        } catch (Exception ex) {
            log.error("zeebe dev ui deploy process.", ex);
            response.setStatusCode(500).end(ex.getMessage());
            return;
        }
        response.setStatusCode(200).end();
    }
}
