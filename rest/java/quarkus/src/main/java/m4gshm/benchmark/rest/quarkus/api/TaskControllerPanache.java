package m4gshm.benchmark.rest.quarkus.api;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.quarkus.service.TaskService;

import static m4gshm.benchmark.rest.quarkus.api.TaskController.PANACHE;

@ApplicationScoped
@Path("/task")
@IfBuildProfile(PANACHE)
public class TaskControllerPanache extends TaskController<TaskEntity> {
    public TaskControllerPanache() {
        this(null);
    }

    @Inject
    public TaskControllerPanache(TaskService<TaskEntity> storage) {
        super(storage);
    }
}
