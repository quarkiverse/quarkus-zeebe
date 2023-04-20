package io.quarkiverse.zeebe.it.bpmn.sayhello;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@Path("say-hello")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SayHelloRestController {

    @Inject
    ZeebeClient client;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response startProcessInstance(SayHelloParameter p) {

        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId("hello_process")
                .latestVersion()
                .variables(p)
                .send().join();

        return Response.ok(event).build();
    }
}
