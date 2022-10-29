package m4gshm.benchmark.rest.quarkus.storage.map;

import io.quarkus.arc.properties.IfBuildProperty;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.storage.MapStorage;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import java.util.concurrent.ConcurrentHashMap;

import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.STORAGE;
import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.STORAGE_VAL_MAP;


@Dependent
@IfBuildProperty(name = STORAGE, stringValue = STORAGE_VAL_MAP, enableIfMissing = true)
public class MapStorageConfiguration {

    @Produces
    @ApplicationScoped
    public Storage<TaskEntity, String> storage() {
        return new MapStorage<>(new ConcurrentHashMap<>());
    }
}
