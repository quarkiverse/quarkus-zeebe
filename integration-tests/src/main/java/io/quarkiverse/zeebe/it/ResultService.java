package io.quarkiverse.zeebe.it;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("result")
public class ResultService {

    private static final Map<Long, Object> DATA = new ConcurrentHashMap<>();

    public static void addResult(Long processInstanceKey, Object data) {
        DATA.put(processInstanceKey, data);
    }

    @GET
    @Path("{processInstanceKey}")
    public Response result(@PathParam("processInstanceKey") Long processInstanceKey) {
        Object result = DATA.getOrDefault(processInstanceKey, null);
        if (result != null) {
            return Response.ok(result).build();
        }
        return Response.noContent().build();
    }
}
