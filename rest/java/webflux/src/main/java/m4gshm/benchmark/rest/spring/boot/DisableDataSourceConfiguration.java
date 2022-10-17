package m4gshm.benchmark.rest.spring.boot;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import static m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepositoryConfiguration.SPRING_DATASOURCE_ENABLED;

@Configuration
@ConditionalOnProperty(name = SPRING_DATASOURCE_ENABLED, havingValue = "false")
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class DisableDataSourceConfiguration {
}
