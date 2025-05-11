package m4gshm.benchmark.rest.quarkus.api;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.quarkus.service.ReactiveTaskService;

import java.util.List;

@RequiredArgsConstructor
public abstract class ReactiveTaskController<T extends Task> {
    public static final String PANACHE = "panache";

    private final ReactiveTaskService<T> service;

    public ReactiveTaskController() {
        this(null);
    }

    @GET
    @Path("/{id}")
    public Uni<Response> get(@PathParam("id") String id) {
        return service.get(id);
    }

    @GET
    public Uni<? extends List<T>> list() {
        return service.list();
    }

    @POST
    public Uni<Status> create(T task) {
        return service.create(task);
    }

    @PUT
    @Path("/{id}")
    public Uni<Status> update(@PathParam("id") String id, T task) {
        return service.update(id, task);
    }

    @DELETE
    @Path("/{id}")
    public Uni<Status> delete(@PathParam("id") String id) {
        return service.delete(id);
    }

}