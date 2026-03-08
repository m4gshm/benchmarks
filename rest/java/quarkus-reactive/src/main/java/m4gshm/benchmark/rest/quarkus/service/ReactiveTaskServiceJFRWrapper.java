package m4gshm.benchmark.rest.quarkus.service;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.jfr.mutiny.JFRStorage;
import m4gshm.benchmark.rest.java.jfr.RestControllerEvent;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.quarkus.api.Status;

import java.util.List;

import static io.smallrye.mutiny.Uni.createFrom;

@RequiredArgsConstructor
public class ReactiveTaskServiceJFRWrapper<T extends Task> implements ReactiveTaskService<T> {

    private final ReactiveTaskService<T> service;

    @Override
    public Uni<Response> get(String id) {
        return rec("get", service.get(id));
    }

    @Override
    public Uni<? extends List<T>> list() {
        return rec("list", service.list());
    }

    @Override
    public Uni<Status> create(T task) {
        return rec("create", service.create(task));
    }

    @Override
    public Uni<Status> update(String id, T task) {
        return rec("update", createFrom().deferred(() -> service.update(id, task)));
    }

    @Override
    public Uni<Status> delete(String id) {
        return rec("delete", createFrom().deferred(() -> service.delete(id)));
    }

    private <V> Uni<V> rec(String name, Uni<V> uni) {
        return JFRStorage.INSTANCE.rec(uni, getClass().getName(), name, RestControllerEvent::create);
    }
}