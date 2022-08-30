package m4gshm.benchmark.rest.quarkus.api;

import io.quarkus.arc.properties.IfBuildProperty;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.jft.RestControllerEvent;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collection;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity.initId;
import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.REACTIVE;
import static m4gshm.benchmark.rest.quarkus.api.Status.OK;

@Path("/task")
@RequiredArgsConstructor
@ApplicationScoped
@IfBuildProperty(name = REACTIVE, stringValue = "false")
public class TaskController {

    @Inject
    private final Storage<TaskEntity, String> storage;
    private final String prefix = "TaskResource.";

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        try (var ignored = RestControllerEvent.start(prefix + "get")) {
            var task = storage().get(id);
            return (task == null ? status(NOT_FOUND) : ok(task)).build();
        }
    }

    @GET
    public Collection<? extends TaskEntity> list() {
        try (var ignored = RestControllerEvent.start(prefix + "list")) {
            return storage().getAll();
        }
    }

    @POST
    public Status create(TaskEntity task) {
        try (var ignored = RestControllerEvent.start(prefix + "create")) {
            var id = initId(task);
            storage().store(task);
            return Status.builder().id(id).success(true).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Status update(@PathParam("id") String id, TaskEntity task) {
        try (var ignored = RestControllerEvent.start(prefix + "update")) {
            storage().store(task);
            return OK;
        }
    }

    @DELETE
    @Path("/{id}")
    public Status delete(@PathParam("id") String id) {
        try (var ignored = RestControllerEvent.start(prefix + "delete")) {
            if (storage().delete(id)) {
                return OK;
            }
            throw new NotFoundException();
        }
    }

    private Storage<TaskEntity, String> storage() {
        return storage;
    }
}