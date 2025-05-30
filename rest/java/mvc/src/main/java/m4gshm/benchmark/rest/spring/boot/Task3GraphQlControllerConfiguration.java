package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.spring.boot.storage.jdbc.TaskStorageJdbcImpl;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.jdbc.TaskStorageQuerydslImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static m4gshm.benchmark.rest.spring.boot.Task1StorageDBConfiguration.NATIVE_JDBC_ENABLED;
import static m4gshm.benchmark.rest.spring.boot.Task1StorageDBConfiguration.QUERYDSL_JDBC_ENABLED;
import static m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepositoryConfiguration.SPRING_DATA_ENABLED;

@Configuration
public class Task3GraphQlControllerConfiguration {

    @Bean
    @ConditionalOnProperty(value = NATIVE_JDBC_ENABLED, havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(TaskStorageJdbcImpl.class)
    public TaskGraphQlController<TaskImpl> taskGraphQlControllerJdbc(Storage<TaskImpl, String> storage) {
        return new TaskGraphQlController<>(storage) {
        };
    }

    @Bean
    @ConditionalOnProperty(value = QUERYDSL_JDBC_ENABLED, havingValue = "true")
    @ConditionalOnBean(TaskStorageQuerydslImpl.class)
    public TaskGraphQlController<TaskImpl> taskGraphQlControllerTaskImplQueryDsl(Storage<TaskImpl, String> storage) {
        return new TaskGraphQlController<>(storage) {
        };
    }

    @Bean
    @ConditionalOnProperty(value = SPRING_DATA_ENABLED, havingValue = "true")
    public TaskGraphQlController<TaskEntity> taskGraphQlControllerTaskEntity(Storage<TaskEntity, String> storage) {
        return new TaskGraphQlController<>(storage) {
        };
    }


}
