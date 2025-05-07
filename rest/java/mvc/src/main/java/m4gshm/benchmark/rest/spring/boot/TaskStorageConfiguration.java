package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.spring.boot.storage.jdbc.TaskStorageJdbcImpl;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.*;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.jdbc.TaskStorageQuerydslImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepositoryConfiguration.SPRING_DATA_ENABLED;

@Configuration(proxyBeanMethods = false)
@EntityScan(basePackageClasses = TaskEntity.class)
public class TaskStorageConfiguration {

    public static final String NATIVE_JDBC_ENABLED = "native.jdbc.enabled";
    public static final String QUERYDSL_JDBC_ENABLED = "querydsl.jdbc.enabled";

    @Bean
    @ConditionalOnProperty(name = NATIVE_JDBC_ENABLED, havingValue = "true", matchIfMissing = true)
    public Storage<TaskImpl, String> taskStorageJdbc(DataSource dataSource) {
        return new TaskStorageJdbcImpl(dataSource);
    }

    @Bean
    @ConditionalOnProperty(name = QUERYDSL_JDBC_ENABLED, havingValue = "true")
    public Storage<TaskImpl, String> taskStorageQuerydsl(DataSource dataSource) {
        return new TaskStorageQuerydslImpl(dataSource);
    }

    @Bean
    @ConditionalOnProperty(name = SPRING_DATA_ENABLED, havingValue = "true")
    public Storage<TaskImpl, String> taskStorageJpa(
            TaskEntityRepository taskEntityRepository, TagEntityRepository tagEntityRepository) {
        return new StorageConverterBasedImpl<>(
                new TaskStorageJpaImpl(taskEntityRepository, tagEntityRepository),
                TaskEntityConvertHelper::toJpa,
                TaskEntityConvertHelper::toImpl
        );
    }

}
