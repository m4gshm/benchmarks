package m4gshm.benchmark.rest.quarkus.storage.db;

import io.quarkus.arc.properties.IfBuildProperty;
import m4gshm.benchmark.rest.java.storage.panache.TaskPanacheRepository;
import m4gshm.benchmark.rest.quarkus.BuildTimeProperties;

import javax.enterprise.context.ApplicationScoped;

public class TaskPanacheRepositoryConfiguration {
    @ApplicationScoped
    @IfBuildProperty(name = BuildTimeProperties.STORAGE, stringValue = BuildTimeProperties.STORAGE_VAL_DB, enableIfMissing = true)
    @IfBuildProperty(name = BuildTimeProperties.REACTIVE, stringValue = "true", enableIfMissing = true)
    public TaskPanacheRepository repository() {
        return new TaskPanacheRepository();
    }
}
