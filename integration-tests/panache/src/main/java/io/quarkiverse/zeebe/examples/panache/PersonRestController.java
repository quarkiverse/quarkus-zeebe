package io.quarkiverse.zeebe.examples.panache;

import java.time.LocalDate;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@Path("person")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PersonRestController {

    @Inject
    PersonRepository repository;

    @Inject
    ZeebeClient client;

    @POST
    public Response create(CreateRequestDTO dto) {

        Map<String, Object> data = Map.of(
                "_name", dto.name,
                "_birth", LocalDate.parse(dto.birth));

        ProcessInstanceEvent event = client.newCreateInstanceCommand()
                .bpmnProcessId("person-process")
                .latestVersion()
                .variables(data)
                .send().join();

        return Response.ok(event).build();
    }

    @GET
    public Response findAll() {
        return Response.ok(repository.findAll().list()).build();
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") String id) {
        return Response.ok(repository.findById(id)).build();
    }

    @GET
    @Path("{name}/name")
    public Response findByName(@PathParam("name") String name) {
        return Response.ok(repository.findByName(name)).build();
    }

    public static class CreateRequestDTO {

        /**
         * name of the person
         */
        public String name;

        /**
         * birth ISO string 2001-08-16
         */
        public String birth;
    };
}
