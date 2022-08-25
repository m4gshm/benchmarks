package m4gshm.benchmark.rest.quarkus.storage.map;

import io.quarkus.arc.properties.IfBuildProperty;
import m4gshm.benchmark.rest.java.storage.MutinyStorage;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.storage.MapStorage;
import m4gshm.benchmark.storage.MutinyMapStorage;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.ConcurrentHashMap;

import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.*;


@IfBuildProperty(name = STORAGE, stringValue = STORAGE_VAL_MAP, enableIfMissing = true)
public class MapStorageConfiguration {

    @Produces
    @ApplicationScoped
    @IfBuildProperty(name = REACTIVE, stringValue = "true", enableIfMissing = true)
    public MutinyStorage<TaskEntity, String> reactiveStorage() {
        return new MutinyMapStorage<>(new ConcurrentHashMap<>());
    }

    @Produces
    @ApplicationScoped
    @IfBuildProperty(name = REACTIVE, stringValue = "false")
    public Storage<TaskEntity, String> storage() {
        return new MapStorage<>(new ConcurrentHashMap<>());
    }
}
