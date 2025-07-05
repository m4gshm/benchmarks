package m4gshm.benchmark.rest.spring.boot;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;


import java.util.concurrent.Executors;

import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static m4gshm.benchmark.rest.spring.boot.VirtualThreadPerTaskExecutorConfiguration.VIRTUAL_THREADS_ENABLED;
import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

@Slf4j
@Configuration
@ConditionalOnProperty(value = VIRTUAL_THREADS_ENABLED, havingValue = "true")
public class VirtualThreadPerTaskExecutorConfiguration {

    public static final String VIRTUAL_THREADS_ENABLED = "virtual-threads.enabled";

    @Bean(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(newVirtualThreadPerTaskExecutor());
    }

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> protocolHandler.setExecutor(newVirtualThreadPerTaskExecutor());
    }

    @PostConstruct
    public void log() {
        log.info("VIRTUAL THREADS ENABLED");
    }
}
