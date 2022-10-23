package m4gshm.benchmark.rest.spring.boot.storage;

import m4gshm.benchmark.rest.java.storage.ReactorStorage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.spring.boot.storage.r2dbc.TaskEntityR2dbcStorage;
import m4gshm.benchmark.rest.spring.boot.storage.r2dbc.TaskEntityRepository;
import m4gshm.benchmark.storage.ReactorMapStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

import static m4gshm.benchmark.rest.spring.boot.storage.r2dbc.TaskEntityRepositoryConfiguration.SPRING_DATASOURCE_ENABLED;

@Configuration(proxyBeanMethods = false)
public class TaskStorageR2dbcConfiguration {

    @Bean
    @ConditionalOnProperty(name = SPRING_DATASOURCE_ENABLED, havingValue = "true")
    ReactorStorage<TaskEntity, String> r2dbcTaskEntityStorage(TaskEntityRepository taskEntityRepository) {
        return new TaskEntityR2dbcStorage(taskEntityRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    ReactorStorage<TaskEntity, String> mapTaskEntityStorage() {
        return new ReactorMapStorage<>(new ConcurrentHashMap<>());
    }
}
