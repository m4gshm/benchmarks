package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.storage.MapStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration(proxyBeanMethods = false)
public class Task1StorageInMemConfiguration {

    public static final String IN_MEM_ENABLED = "in-mem.enabled";

    @Bean
    @ConditionalOnProperty(value = IN_MEM_ENABLED, havingValue = "true")
    public Storage<TaskImpl, String> taskStorageInMem() {
        return new MapStorage<>(new ConcurrentHashMap<>());
    }

}
