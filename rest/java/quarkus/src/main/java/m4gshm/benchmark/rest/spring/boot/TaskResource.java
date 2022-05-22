package m4gshm.benchmark.rest.spring.boot;

import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.model.Task;
import m4gshm.benchmark.storage.Storage;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

@Path("/task")
@RequiredArgsConstructor
public class TaskResource {

    private static final Status OK = new Status(true);

    private final Storage<Task, String> storage;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive";
    }

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Response get(@PathParam("id") String id) {
        var task = storage.get(id);
        return (task == null ? status(NOT_FOUND) : ok(task)).build();
    }

    @Produces(APPLICATION_JSON)
    public Collection<Task> list() {
        return storage.getAll();
    }

    @POST
    @Produces(APPLICATION_JSON)
    public Status create(Task task) {
        var id = task.getId();
        if (id == null) task.setId(id = UUID.randomUUID().toString());
        storage.store(id, task);
        return OK;
    }

    @PUT
    @Path("/{id}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Status update(@PathParam("id") String id, Task task) {
        if (task.getId() == null) {
            task.setId(id);
        }
        storage.store(id, task);
        return OK;
    }

    @DELETE
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public Status delete(@PathParam("id") String id) {
        if (storage.delete(id)) {
            return OK;
        }
        throw new NotFoundException();
    }

    public record Status(boolean success) {
    }
}