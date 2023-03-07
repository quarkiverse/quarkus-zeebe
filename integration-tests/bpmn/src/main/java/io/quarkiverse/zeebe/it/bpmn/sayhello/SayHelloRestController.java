package io.quarkiverse.zeebe.it.bpmn.sayhello;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
