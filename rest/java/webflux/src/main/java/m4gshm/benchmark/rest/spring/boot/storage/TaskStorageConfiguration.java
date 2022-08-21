package m4gshm.benchmark.rest.spring.boot.storage;

import m4gshm.benchmark.rest.java.model.Task;
import m4gshm.benchmark.storage.MapStorage;
import m4gshm.benchmark.storage.Storage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class TaskStorageConfiguration {

    @Bean
    public <T extends Task<T, D>, D extends OffsetDateTime> Storage<T, String> storage() {
        return new MapStorage<>(new ConcurrentHashMap<>());
    }
}
