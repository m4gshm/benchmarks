package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.TagEntityRepository;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityJpaStorage;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepository;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.jdbc.TaskRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

import static m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepositoryConfiguration.SPRING_DATA_ENABLED;

@Configuration(proxyBeanMethods = false)
@EntityScan(basePackageClasses = TaskEntity.class)
public class TaskStorageConfiguration {

    public static final String QUERYDSL_JDBC_ENABLED = "querydsl.jdbc.enabled";

    @Bean
    @ConditionalOnProperty(name = QUERYDSL_JDBC_ENABLED, havingValue = "true")
    public Storage<TaskImpl, String> querydslJdbcTaskEntityStorage(DataSource dataSource) {
        return new TaskRepositoryImpl(dataSource);
    }

    @Bean
    @ConditionalOnProperty(name = SPRING_DATA_ENABLED, havingValue = "true")
    public Storage<TaskImpl, String> jpaTaskEntityStorage(
            TaskEntityRepository taskEntityRepository, TagEntityRepository tagEntityRepository) {
        var taskEntityJpaStorage = new TaskEntityJpaStorage(taskEntityRepository, tagEntityRepository);
        return new Storage<>() {

            private TaskImpl toImpl(TaskEntity taskEntity) {
                return TaskImpl.builder()
                        .id(taskEntity.getId())
                        .deadline(taskEntity.getDeadline())
                        .text(taskEntity.getText())
                        .tags(taskEntity.getTags())
                        .build();
            }


            private TaskEntity toJpa(TaskImpl entity) {
                var task = TaskEntity.builder()
                        .id(entity.id())
                        .text(entity.text())
                        .deadline(entity.deadline())
                        .build();
                task.setTags(entity.getTags());
                return task;
            }

            @Override
            public TaskImpl get(String id) {
                return toImpl(taskEntityJpaStorage.get(id));
            }

            @Override
            public List<TaskImpl> getAll() {
                return taskEntityJpaStorage.getAll().stream().map(this::toImpl).toList();
            }

            @Override
            public TaskImpl store(TaskImpl entity) {
                return toImpl(taskEntityJpaStorage.store(toJpa(entity)));
            }

            @Override
            public boolean delete(String id) {
                return taskEntityJpaStorage.delete(id);
            }
        };
    }

}
