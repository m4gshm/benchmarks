package m4gshm.benchmark.rest.quarkus.api;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.quarkus.service.TaskService;

import java.util.Collection;

@Path("/task")
@ApplicationScoped
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    public Response get(@PathParam("id") String id) {
        return service.get(id);
    }

    @GET
    @RunOnVirtualThread
    public Collection<? extends TaskEntity> list() {
        return service.list();
    }

    @POST
    @RunOnVirtualThread
    public Status create(TaskEntity task) {
        return service.create(task);
    }

    @PUT
    @Path("/{id}")
    @RunOnVirtualThread
    public Status update(@PathParam("id") String id, TaskEntity task) {
        return service.update(id, task);
    }

    @DELETE
    @Path("/{id}")
    @RunOnVirtualThread
    public Status delete(@PathParam("id") String id) {
        return service.delete(id);
    }

}