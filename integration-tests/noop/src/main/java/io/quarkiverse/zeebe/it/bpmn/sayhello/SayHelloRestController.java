package io.quarkiverse.zeebe.it.bpmn.sayhello;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;

@Path("say-hello")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SayHelloRestController {

    @Inject
    ZeebeClient client;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response startProcessInstance(SayHelloParameter p) {

        ProcessInstanceResult event = client
                .newCreateInstanceCommand()
                .bpmnProcessId("hello_process")
                .latestVersion()
                .variables(p)
                .withResult()
                .send()
                .join(5, TimeUnit.SECONDS);

        return Response.ok(event).build();
    }
}
