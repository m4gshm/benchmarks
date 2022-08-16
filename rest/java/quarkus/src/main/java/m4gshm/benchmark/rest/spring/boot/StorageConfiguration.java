package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.concurrency.RwLockMap;
import m4gshm.benchmark.rest.java.model.Task;
import m4gshm.benchmark.storage.MapStorage;
import m4gshm.benchmark.storage.Storage;

import javax.enterprise.inject.Produces;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class StorageConfiguration {
    @Produces
    public Storage<Task, String> storage() {
//        return new MapStorage<>(new RwLockMap<>(new HashMap<>()));
        return new MapStorage<>(new ConcurrentHashMap<>());
    }
}
