package io.quarkiverse.zeebe.examples.opentelemetry;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@Path("process")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProcessRestController {

    @Inject
    ZeebeClient zeebe;

    @POST
    @Path("start/{processId}")
    public Response startProcess(@PathParam("processId") String processId, Map<String, Object> parameters) {
        try {
            ProcessInstanceEvent event = zeebe.newCreateInstanceCommand()
                    .bpmnProcessId(processId)
                    .latestVersion()
                    .variables(parameters)
                    .send().join();

            return Response.ok(event).build();
        } catch (Exception ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

}
