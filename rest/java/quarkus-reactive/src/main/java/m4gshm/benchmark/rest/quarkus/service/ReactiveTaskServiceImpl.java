package m4gshm.benchmark.rest.quarkus.service;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.jfr.mutiny.JFRStorage;
import m4gshm.benchmark.rest.java.jfr.RestControllerEvent;
import m4gshm.benchmark.rest.java.storage.MutinyStorage;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.quarkus.api.Status;

import java.util.List;

import static io.smallrye.mutiny.Uni.createFrom;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.ok;
import static jakarta.ws.rs.core.Response.status;
import static m4gshm.benchmark.rest.quarkus.api.Status.OK;

@RequiredArgsConstructor
public class ReactiveTaskServiceImpl<T extends Task> implements ReactiveTaskService<T> {

    private final MutinyStorage<T, String> storage;

    @Override
    public Uni<Response> get(String id) {
        return rec("get", storage.get(id)
                .map(entity -> entity != null ? ok(entity) : status(NOT_FOUND))
                .map(Response.ResponseBuilder::build));
    }

    @Override
    public Uni<? extends List<T>> list() {
        return rec("list", storage.getAll());
    }

    @Override
    public Uni<Status> create(T task) {
        return rec("create", storage.store(task.getId(), task).map(entity ->
                Status.builder().id(entity.getId()).success(true).build())
        );
    }

    @Override
    public Uni<Status> update(String id, T task) {
        return rec("update", createFrom().deferred(() -> storage.store(id, task).map(entity -> OK)));
    }

    @Override
    public Uni<Status> delete(String id) {
        return rec("delete", createFrom().deferred(() -> storage.delete(id).flatMap(delete -> delete
                ? createFrom().item(OK)
                : createFrom().failure(new NotFoundException()))));
    }

    private <V> Uni<V> rec(String name, Uni<V> uni) {
        return JFRStorage.INSTANCE.rec(uni, getClass().getName(), name, RestControllerEvent::create);
    }

}