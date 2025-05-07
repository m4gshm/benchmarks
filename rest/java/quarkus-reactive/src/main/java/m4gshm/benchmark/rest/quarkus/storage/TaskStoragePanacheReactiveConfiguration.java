package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import m4gshm.benchmark.rest.java.storage.MutinyStorage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;

import static m4gshm.benchmark.rest.quarkus.storage.TaskStoragePanacheReactiveConfiguration.QUARKUS_HIBERNATE_ORM_ACTIVE;


@Dependent
@LookupIfProperty(name = QUARKUS_HIBERNATE_ORM_ACTIVE, stringValue = "true")
public class TaskStoragePanacheReactiveConfiguration {

    public static final String QUARKUS_HIBERNATE_ORM_ACTIVE = "quarkus.hibernate-orm.active";

    @Produces
    @ApplicationScoped
    public TaskPanacheRepository taskPanacheRepository() {
        return new TaskPanacheRepository();
    }

    @ApplicationScoped
    public MutinyStorage<TaskEntity, String> taskStoragePanacheReactive(TaskPanacheRepository taskPanacheRepository) {
        return new TaskStoragePanacheReactiveImpl(taskPanacheRepository);
    }

}
