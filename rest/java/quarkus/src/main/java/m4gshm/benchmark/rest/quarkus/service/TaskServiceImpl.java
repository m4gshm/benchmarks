package m4gshm.benchmark.rest.quarkus.service;

import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.quarkus.api.Status;

import java.util.Collection;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.ok;
import static jakarta.ws.rs.core.Response.status;
import static m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity.initId;
import static m4gshm.benchmark.rest.quarkus.api.Status.OK;

@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final Storage<TaskEntity, String> storage;

    @Override
    public Response get(String id) {
        var task = storage().get(id);
        return (task == null ? status(NOT_FOUND) : ok(task)).build();
    }

    @Override
    public Collection<TaskEntity> list() {
        return storage().getAll();
    }

    @Override
    public Status create(TaskEntity task) {
        var stored = storage().store(initId(task));
        return Status.builder().id(stored.getId()).success(true).build();
    }

    @Override
    public Status update(String id, TaskEntity task) {
        storage().store(task.withId(id));
        return OK;
    }

    @Override
    public Status delete(String id) {
        if (storage().delete(id)) {
            return OK;
        }
        throw new NotFoundException();
    }

    private Storage<TaskEntity, String> storage() {
        return storage;
    }
}