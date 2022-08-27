package m4gshm.benchmark.rest.quarkus.api;

import io.quarkus.arc.properties.IfBuildProperty;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.jfr.JFR;
import m4gshm.benchmark.rest.java.storage.MutinyStorage;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.List;

import static io.smallrye.mutiny.Uni.createFrom;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity.initId;
import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.REACTIVE;
import static m4gshm.benchmark.rest.quarkus.api.Status.OK;

@Path("/task")
@RequiredArgsConstructor
@ApplicationScoped
@IfBuildProperty(name = REACTIVE, stringValue = "true", enableIfMissing = true)
public class ReactiveTaskController {

    private final MutinyStorage<TaskEntity, String> storage;

    @GET
    @Path("/{id}")
    public Uni<Response> get(@PathParam("id") String id) {
        return rec("get", storage.get(id)
                .map(entity -> entity != null ? ok(entity): status(NOT_FOUND))
                .map(ResponseBuilder::build));
    }

    @GET
    public Uni<? extends List<? extends Task<?>>> list() {
        return rec("list", storage.getAll());
    }

    @POST
    public Uni<Status> create(TaskEntity task) {
        return rec("create", createFrom().deferred(() -> {
            var id = initId(task);
            return storage.store(task).map(entity -> Status.builder().id(id).success(true).build());
        }));
    }

    @PUT
    @Path("/{id}")
    public Uni<Status> update(@PathParam("id") String id, TaskEntity task) {
        task.setId(id);
        return rec("update", createFrom().deferred(() -> storage.store(task).map(entity -> OK)));
    }

    @DELETE
    @Path("/{id}")
    public Uni<Status> delete(@PathParam("id") String id) {
        return rec("delete", createFrom().deferred(() -> storage.delete(id).flatMap(delete -> delete
                ? createFrom().item(OK)
                : createFrom().failure(new NotFoundException()))));
    }

    private <T> Uni<T> rec(String name, Uni<T> uni) {
        return JFR.INSTANCE.rec(uni, getClass().getName(), name);
    }

}