package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import static m4gshm.benchmark.rest.spring.boot.storage.r2dbc.TaskEntityRepositoryConfiguration.SPRING_DATASOURCE_ENABLED;

@Configuration
@EnableR2dbcRepositories(basePackageClasses = TaskEntityRepository.class)
@EntityScan(basePackageClasses = TaskEntity.class)
@ConditionalOnProperty(name = SPRING_DATASOURCE_ENABLED, havingValue = "true")
public class TaskEntityRepositoryConfiguration {
    public static final String SPRING_DATASOURCE_ENABLED = "spring.datasource.enabled";

}
