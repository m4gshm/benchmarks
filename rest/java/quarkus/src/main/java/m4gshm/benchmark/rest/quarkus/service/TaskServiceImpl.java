package m4gshm.benchmark.rest.quarkus.service;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.quarkus.api.Status;

import java.util.Collection;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.ok;
import static jakarta.ws.rs.core.Response.status;
import static m4gshm.benchmark.rest.quarkus.api.Status.OK;

@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService<Task> {

    private final Storage<Task, String> storage;

    @Override
    public Response get(String id) {
        var task = storage.get(id);
        return (task == null ? status(NOT_FOUND) : ok(task)).build();
    }

    @Override
    public Collection<Task> list() {
        return storage.getAll();
    }

    @Override
    public Status create(Task task) {
        var stored = storage.store(task, task.getId());
        return Status.builder().id(stored.getId()).success(true).build();
    }

    @Override
    public Status update(String id, Task task) {
        storage.store(task, task.getId());
        return OK;
    }

    @Override
    public Status delete(String id) {
        if (storage.delete(id)) {
            return OK;
        }
        throw new NotFoundException();
    }
}