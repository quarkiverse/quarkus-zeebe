package io.quarkiverse.zeebe.examples.reactive;

import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@Path("/test")
@RegisterRestClient(configKey = "test")
public interface TestRestClient {

    @GET
    CompletionStage<String> completionStage();

    @GET
    Uni<String> uni();

    @GET
    String blocking();
}
