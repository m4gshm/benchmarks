package m4gshm.benchmark.rest.spring.boot.api;

import io.quarkus.arc.properties.IfBuildProperty;
import io.smallrye.mutiny.Uni;
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

import static io.smallrye.mutiny.Uni.createFrom;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

@Path("/task")
@RequiredArgsConstructor
@ApplicationScoped
@IfBuildProperty(name = "reactive", stringValue = "true")
public class ReactiveTaskController {

    private static final Status OK = new Status(true);

    private final Storage<Task, String> storage;
    private final String prefix = "TaskResource.";

    @GET
    @Path("/{id}")
    public Uni<Response> get(@PathParam("id") String id) {
        return createFrom().item(() -> {
            try (var ignored = RestControllerEvent.start(prefix + "get")) {
                var task = storage.get(id);
                return (task == null ? status(NOT_FOUND) : ok(task)).build();
            }
        });
    }

    @GET
    public Uni<Collection<Task>> list() {
        return createFrom().item(() -> {
            try (var ignored = RestControllerEvent.start(prefix + "list")) {
                return storage.getAll();
            }
        });
    }

    @POST
    public Uni<Status> create(TaskImpl task) {
        return createFrom().item(() -> {
            try (var ignored = RestControllerEvent.start(prefix + "create")) {
                var id = task.id();
                var t = task;
                if (id == null) t = task.withId(id = UUID.randomUUID().toString());
                storage.store(id, t);
                return OK;
            }
        });
    }

    @PUT
    @Path("/{id}")
    public Uni<Status> update(@PathParam("id") String id, TaskImpl task) {
        return createFrom().item(() -> {
            try (var ignored = RestControllerEvent.start(prefix + "update")) {
                storage.store(id, task.id() == null ? task.withId(id) : task);
                return OK;
            }
        });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Status> delete(@PathParam("id") String id) {
        return createFrom().item(() -> {
            try (var ignored = RestControllerEvent.start(prefix + "delete")) {
                if (storage.delete(id)) {
                    return OK;
                }
                throw new NotFoundException();
            }
        });
    }

    public record Status(boolean success) {
    }
}