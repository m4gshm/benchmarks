package m4gshm.benchmark.rest.quarkus.api;

import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.quarkus.service.TaskService;

import static m4gshm.benchmark.rest.quarkus.api.TaskController.PANACHE;

@ApplicationScoped
@Path("/task")
@UnlessBuildProfile(anyOf = PANACHE)
public class TaskControllerSql extends TaskController<TaskImpl> {
    public TaskControllerSql() {
        this(null);
    }

    @Inject
    public TaskControllerSql(TaskService<TaskImpl> storage) {
        super(storage);
    }
}
