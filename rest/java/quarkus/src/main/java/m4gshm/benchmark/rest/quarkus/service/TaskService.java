package m4gshm.benchmark.rest.quarkus.service;

import jakarta.ws.rs.core.Response;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.quarkus.api.Status;

import java.util.Collection;

public interface TaskService<T extends Task> {
    Response get(String id);

    Collection<T> list();

    Status create(T task);

    Status update(String id, T task);

    Status delete(String id);
}
