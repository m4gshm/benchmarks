package m4gshm.benchmark.rest.quarkus.api;

import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.quarkus.service.ReactiveTaskService;

import static m4gshm.benchmark.rest.quarkus.api.ReactiveTaskController.PANACHE;

@ApplicationScoped
@Path("/task")
@UnlessBuildProfile(anyOf = PANACHE)
public class ReactiveTaskControllerSql extends ReactiveTaskController<TaskImpl> {
    @Inject
    public ReactiveTaskControllerSql(ReactiveTaskService<TaskImpl> service) {
        super(service);
    }
}
