package m4gshm.benchmark.rest.spring.boot;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import static m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepositoryConfiguration.SPRING_DATASOURCE_ENABLED;

@Configuration(proxyBeanMethods = false)
public class DataSourceConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = SPRING_DATASOURCE_ENABLED, havingValue = "true")
    @EnableAutoConfiguration
    public static class Enable {
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = SPRING_DATASOURCE_ENABLED, havingValue = "false", matchIfMissing = true)
    @EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
    public static class Disable {
    }
}
