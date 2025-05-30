package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.spring.boot.storage.jdbc.TaskStorageJdbcImpl;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.TagEntityRepository;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepository;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskStorageJpaImpl;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.jdbc.TaskStorageQuerydslImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.sql.DataSource;

import static m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepositoryConfiguration.SPRING_DATA_ENABLED;

@Configuration(value = "taskStorageDBConfiguration", proxyBeanMethods = false)
@EntityScan(basePackageClasses = TaskEntity.class)
public class Task1StorageDBConfiguration {

    public static final String NATIVE_JDBC_ENABLED = "native.jdbc.enabled";
    public static final String QUERYDSL_JDBC_ENABLED = "querydsl.jdbc.enabled";
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE;

    @Bean
    @ConditionalOnProperty(name = NATIVE_JDBC_ENABLED, havingValue = "true")
    public TaskStorageJdbcImpl taskStorageJdbc(DataSource dataSource) {
        return new TaskStorageJdbcImpl(dataSource);
    }

    @Bean
    @ConditionalOnProperty(name = QUERYDSL_JDBC_ENABLED, havingValue = "true")
    public TaskStorageQuerydslImpl taskStorageQuerydsl(DataSource dataSource) {
        return new TaskStorageQuerydslImpl(dataSource);
    }

    @Bean
    @ConditionalOnProperty(name = SPRING_DATA_ENABLED, havingValue = "true")
    public Storage<TaskEntity, String> taskStorageJpa(TaskEntityRepository taskEntityRepository,
                                                      TagEntityRepository tagEntityRepository) {
        return new TaskStorageJpaImpl(taskEntityRepository, tagEntityRepository);
    }

}
