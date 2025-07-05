package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.storage.MapStorage;

import java.util.concurrent.ConcurrentHashMap;

@Dependent
public class InMemoryStorageConfiguration {
    @Produces
    @ApplicationScoped
    @DefaultBean
    public Storage<TaskEntity, String> mapStorage() {
        return new MapStorage<>(new ConcurrentHashMap<>());
    }

}
