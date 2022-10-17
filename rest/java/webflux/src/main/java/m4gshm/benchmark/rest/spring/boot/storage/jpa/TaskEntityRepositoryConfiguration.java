package m4gshm.benchmark.rest.spring.boot.storage.jpa;

import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepositoryConfiguration.SPRING_DATASOURCE_ENABLED;

@Configuration
@EnableJpaRepositories(basePackageClasses = TaskEntityRepository.class)
@EntityScan(basePackageClasses = TaskEntity.class)
@ConditionalOnProperty(name = SPRING_DATASOURCE_ENABLED, havingValue = "true")
public class TaskEntityRepositoryConfiguration {
    public static final String SPRING_DATASOURCE_ENABLED = "spring.datasource.enabled";

}
