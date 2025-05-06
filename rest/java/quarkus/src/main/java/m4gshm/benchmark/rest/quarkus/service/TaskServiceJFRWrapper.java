package m4gshm.benchmark.rest.quarkus.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.quarkus.api.Status;

import java.util.Collection;

@RequiredArgsConstructor
public class TaskServiceJFRWrapper implements TaskService {

    private final TaskService service;

    @Override
    public Response get(String id) {
        return service.get(id);
    }

    @Override
    public Collection<TaskEntity> list() {
        return service.list();
    }

    @Override
    public Status create(TaskEntity task) {
        return service.create(task);
    }

    @Override
    public Status update(String id, TaskEntity task) {
        return service.update(id, task);
    }

    @Override
    public Status delete(String id) {
        return service.delete(id);
    }

}