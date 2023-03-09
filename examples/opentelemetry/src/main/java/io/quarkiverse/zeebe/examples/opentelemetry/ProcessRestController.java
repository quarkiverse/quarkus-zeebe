package io.quarkiverse.zeebe.examples.opentelemetry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
