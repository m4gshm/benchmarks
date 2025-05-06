package m4gshm.benchmark.rest.quarkus.service;

import jakarta.ws.rs.core.Response;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.quarkus.api.Status;

import java.util.Collection;

public interface TaskService {
    Response get(String id);

    Collection<TaskEntity> list();

    Status create(TaskEntity task);

    Status update(String id, TaskEntity task);

    Status delete(String id);
}
