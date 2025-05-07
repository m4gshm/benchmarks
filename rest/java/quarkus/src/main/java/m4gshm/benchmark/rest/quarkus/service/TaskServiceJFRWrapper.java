package m4gshm.benchmark.rest.quarkus.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.quarkus.api.Status;

import java.util.Collection;

@RequiredArgsConstructor
public class TaskServiceJFRWrapper<T extends Task> implements TaskService<T> {

    private final TaskService<T> service;

    @Override
    public Response get(String id) {
        return service.get(id);
    }

    @Override
    public Collection<T> list() {
        return service.list();
    }

    @Override
    public Status create(T task) {
        return service.create(task);
    }

    @Override
    public Status update(String id, T task) {
        return service.update(id, task);
    }

    @Override
    public Status delete(String id) {
        return service.delete(id);
    }

}