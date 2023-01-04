package m4gshm.benchmark.rest.spring.boot.storage;

import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.TagEntityRepository;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityJpaStorage;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepository;
import m4gshm.benchmark.storage.MapStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

import static m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepositoryConfiguration.SPRING_DATASOURCE_ENABLED;

@Configuration(proxyBeanMethods = false)
public class TaskStorageJpaConfiguration {

    @Bean
    @ConditionalOnProperty(name = SPRING_DATASOURCE_ENABLED, havingValue = "true")
    Storage<TaskEntity, String> jpaTaskEntityStorage(
            TaskEntityRepository taskEntityRepository, TagEntityRepository tagEntityRepository) {
        return new TaskEntityJpaStorage(taskEntityRepository, tagEntityRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    Storage<TaskEntity, String> mapTaskEntityStorage() {
        return new MapStorage<>(new ConcurrentHashMap<>(1000));
    }
}
