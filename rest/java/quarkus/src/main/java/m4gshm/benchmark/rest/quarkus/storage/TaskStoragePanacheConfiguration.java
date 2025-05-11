package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;


@Dependent
//@LookupIfProperty(name = TaskStoragePanacheConfiguration.QUARKUS_HIBERNATE_ORM_ACTIVE, stringValue = "true")
public class TaskStoragePanacheConfiguration {

    public static final String QUARKUS_HIBERNATE_ORM_ACTIVE = "quarkus.hibernate-orm.active";

    @Produces
    @ApplicationScoped
    public TaskPanacheRepository taskPanacheRepository() {
        return new TaskPanacheRepository();
    }

    @Produces
    @ApplicationScoped
    public TagPanacheRepository tagPanacheRepository() {
        return new TagPanacheRepository();
    }

    @Produces
    @ApplicationScoped
    public TaskStoragePanacheImpl taskStorage(TaskPanacheRepository taskRepo, TagPanacheRepository tagRepo) {
        return new TaskStoragePanacheImpl(taskRepo, tagRepo);
    }

}
