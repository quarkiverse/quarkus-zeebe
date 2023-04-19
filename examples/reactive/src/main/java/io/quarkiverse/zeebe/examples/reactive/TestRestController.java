package io.quarkiverse.zeebe.examples.reactive;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.client.ZeebeClient;

@Path("test")
public class TestRestController {

    private static Logger log = LoggerFactory.getLogger(TestRestController.class);

    @Inject
    ZeebeClient client;

    @Inject
    JobCounter jobCounter;

    @GET
    @Path("reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset() {
        jobCounter.reset();
        return Response.ok().build();
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createInstances(@PathParam("name") String name) {
        log.info("Start process instances... '{}'", name);
        for (int i = 0; i < 500; i++) {
            client.newCreateInstanceCommand()
                    .bpmnProcessId(name)
                    .latestVersion()
                    .send().join();
        }
        log.info("...done");
        return Response.ok().build();
    }
}
