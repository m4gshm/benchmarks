package m4gshm.benchmark.rest.spring.boot.api;

import io.quarkus.arc.properties.IfBuildProperty;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.jft.RestControllerEvent;
import m4gshm.benchmark.rest.java.model.Task;
import m4gshm.benchmark.rest.java.model.TaskImpl;
import m4gshm.benchmark.storage.Storage;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

@Path("/task")
@RequiredArgsConstructor
@ApplicationScoped
@IfBuildProperty(name = "reactive", stringValue = "false")
public class TaskController {

    private static final Status OK = new Status(true);

    private final Storage<Task, String> storage;
    private final String prefix = "TaskResource.";

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        try (var ignored = RestControllerEvent.start(prefix + "get")) {
            var task = storage.get(id);
            return (task == null ? status(NOT_FOUND) : ok(task)).build();
        }
    }

    @GET
    public Collection<Task> list() {
        try (var ignored = RestControllerEvent.start(prefix + "list")) {
            return storage.getAll();
        }
    }

    @POST
    public Status create(TaskImpl task) {
        try (var ignored = RestControllerEvent.start(prefix + "create")) {
            var id = task.id();
            var t = task;
            if (id == null) t = task.withId(id = UUID.randomUUID().toString());
            storage.store(id, t);
            return OK;
        }
    }

    @PUT
    @Path("/{id}")
    public Status update(@PathParam("id") String id, TaskImpl task) {
        try (var ignored = RestControllerEvent.start(prefix + "update")) {
            storage.store(id, task.withId(id));
            return OK;
        }
    }

    @DELETE
    @Path("/{id}")
    public Status delete(@PathParam("id") String id) {
        try (var ignored = RestControllerEvent.start(prefix + "delete")) {
            if (storage.delete(id)) {
                return OK;
            }
            throw new NotFoundException();
        }
    }

    public record Status(boolean success) {
    }
}