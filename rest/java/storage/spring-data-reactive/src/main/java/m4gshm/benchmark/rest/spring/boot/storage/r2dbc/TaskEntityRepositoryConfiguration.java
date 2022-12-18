package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import io.r2dbc.spi.ConnectionFactory;
import m4gshm.benchmark.rest.spring.boot.storage.r2dbc.model.TaskEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static m4gshm.benchmark.rest.spring.boot.storage.r2dbc.TaskEntityRepositoryConfiguration.SPRING_DATASOURCE_ENABLED;

@Configuration
//@EnableR2dbcRepositories(basePackageClasses = TaskEntityRepository.class)
@EntityScan(basePackageClasses = TaskEntity.class)
@ConditionalOnProperty(name = SPRING_DATASOURCE_ENABLED, havingValue = "true")
public class TaskEntityRepositoryConfiguration {
    public static final String SPRING_DATASOURCE_ENABLED = "spring.datasource.enabled";

    @Bean
    TaskEntityRepositoryImpl taskEntityRepository(ConnectionFactory connectionFactory) {
        return new TaskEntityRepositoryImpl(connectionFactory);
    }

}
