package m4gshm.benchmark.rest.quarkus.api;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.quarkus.service.ReactiveTaskService;

import static m4gshm.benchmark.rest.quarkus.api.ReactiveTaskController.PANACHE;

@ApplicationScoped
@Path("/task")
@IfBuildProfile(PANACHE)
public class ReactiveTaskControllerPanache extends ReactiveTaskController<TaskEntity> {
    @Inject
    public ReactiveTaskControllerPanache(ReactiveTaskService<TaskEntity> service) {
        super(service);
    }
}
