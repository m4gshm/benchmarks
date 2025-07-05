package m4gshm.benchmark.rest.quarkus.service;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.quarkus.api.Status;

import java.util.List;

public interface ReactiveTaskService<T extends Task> {
    Uni<Response> get(String id);

    Uni<? extends List<T>> list();

    Uni<Status> create(T task);

    Uni<Status> update(String id, T task);

    Uni<Status> delete(String id);
}
