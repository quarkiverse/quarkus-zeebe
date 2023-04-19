package io.quarkiverse.zeebe.examples.opentelemetry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@Path("process/test")
@Produces(MediaType.APPLICATION_JSON)
public class ProcessRestController {

    @Inject
    ZeebeClient zeebe;

    @GET
    public Response startTest() {
        List<ProcessInstanceEvent> result = new ArrayList<>();
        try {
            result.add(start("test.complete"));
            result.add(start("test.exception"));
            result.add(start("test.fail"));
            result.add(start("test.throw"));
            return Response.ok(result).build();
        } catch (Exception ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("{name}")
    public Response startTest(@PathParam("name") String name) {
        try {
            return Response.ok(start(name)).build();
        } catch (Exception ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    private ProcessInstanceEvent start(String processId) {
        return zeebe.newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(Map.of("data", "1"))
                .send().join();
    }
}
