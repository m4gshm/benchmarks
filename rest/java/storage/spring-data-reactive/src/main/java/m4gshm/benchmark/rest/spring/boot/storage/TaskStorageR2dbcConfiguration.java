package m4gshm.benchmark.rest.spring.boot.storage;

import io.r2dbc.spi.ConnectionFactory;
import m4gshm.benchmark.rest.java.storage.ReactorStorage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.r2dbc.TaskRepositoryImpl;
import m4gshm.benchmark.rest.spring.boot.storage.r2dbc.TaskR2dbcRepositoryImpl;
import m4gshm.benchmark.rest.spring.boot.storage.r2dbc.TaskReactiveStorageImpl;
import m4gshm.benchmark.storage.ReactorMapStorage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration(proxyBeanMethods = false)
public class TaskStorageR2dbcConfiguration {

    public static final String QUERYDSL_R2DBC_ENABLED = "querydsl.r2dbc.enabled";

    @Bean
    public ReactorStorage<TaskImpl, String> querydslR2dbcTaskEntityStorage(
            ObjectProvider<ConnectionFactory> connectionFactory,
            @Value("${" + QUERYDSL_R2DBC_ENABLED + ":false}") boolean queryDsl
    ) {
        var available = connectionFactory.getIfAvailable();
        return available == null ? new ReactorMapStorage<>(new ConcurrentHashMap<>())
                : queryDsl ? new TaskRepositoryImpl(available) : new TaskReactiveStorageImpl(new TaskR2dbcRepositoryImpl(available));
    }

}
