package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.arc.lookup.LookupUnlessProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.storage.MapStorage;

import java.util.concurrent.ConcurrentHashMap;


@Dependent
public class StorageConfiguration {

    public static final String QUARKUS_HIBERNATE_ORM_ACTIVE = "quarkus.hibernate-orm.active";

    @Produces
    @ApplicationScoped
    @LookupIfProperty(name = QUARKUS_HIBERNATE_ORM_ACTIVE, stringValue = "false")
    public Storage<TaskEntity, String> mapStorage() {
        return new MapStorage<>(new ConcurrentHashMap<>());
    }

    @Produces
    @ApplicationScoped
    @LookupUnlessProperty(name = QUARKUS_HIBERNATE_ORM_ACTIVE, stringValue = "false")
    public TaskPanacheRepository taskPanacheRepository() {
        return new TaskPanacheRepository();
    }

    @Produces
    @ApplicationScoped
    @LookupUnlessProperty(name = QUARKUS_HIBERNATE_ORM_ACTIVE, stringValue = "false")
    public TagPanacheRepository tagPanacheRepository() {
        return new TagPanacheRepository();
    }


}
