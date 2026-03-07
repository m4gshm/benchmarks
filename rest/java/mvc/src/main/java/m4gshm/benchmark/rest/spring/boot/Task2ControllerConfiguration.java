package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.spring.boot.storage.jdbc.TaskStorageJdbcImpl;
import m4gshm.benchmark.rest.spring.boot.storage.jdbc.TaskStorageJdbcJoinedQueriesImpl;
import m4gshm.benchmark.rest.spring.boot.storage.jooq.jdbc.TaskStorageJooqImpl;
import m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskStorageJpaImpl;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.jdbc.TaskStorageQuerydslImpl;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import static m4gshm.benchmark.rest.spring.boot.Task1StorageDBConfiguration.*;
import static m4gshm.benchmark.rest.spring.boot.storage.jpa.TaskEntityRepositoryConfiguration.SPRING_DATA_ENABLED;
import static org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN;

@Configuration
public class Task2ControllerConfiguration {

    @Bean
    @Conditional(JdbcCondition.class)
    public TaskController<TaskImpl> taskControllerJdbc(Storage<TaskImpl, String> storage) {
        return new TaskController<>(storage) {
        };
    }

//    @Bean
//    @ConditionalOnProperty(value = NATIVE_JDBC_V2_ENABLED, havingValue = "true", matchIfMissing = true)
//    @ConditionalOnBean(TaskStorageJdbcJoinedQueriesImpl.class)
//    public TaskController<TaskImpl> taskControllerJdbcV2(Storage<TaskImpl, String> storage) {
//        return new TaskController<>(storage) {
//        };
//    }
//
//    @Bean
//    @ConditionalOnProperty(value = QUERYDSL_JDBC_ENABLED, havingValue = "true")
//    @ConditionalOnBean(TaskStorageQuerydslImpl.class)
//    public TaskController<TaskImpl> taskControllerTaskImplQueryDsl(Storage<TaskImpl, String> storage) {
//        return new TaskController<>(storage) {
//        };
//    }
//
//    @Bean
//    @ConditionalOnProperty(value = SPRING_DATA_ENABLED, havingValue = "true")
//    public TaskController<TaskEntity> taskControllerTaskEntity(Storage<TaskEntity, String> storage) {
//        return new TaskController<>(storage) {
//        };
//    }

    public static class JdbcCondition extends AnyNestedCondition {
        public JdbcCondition() {
            super(REGISTER_BEAN);
        }

        @ConditionalOnBean(TaskStorageJooqImpl.class)
        static class JooqJdbcMode {

        }

        @ConditionalOnBean(TaskStorageJdbcImpl.class)
        static class NativeJdbcMode {

        }

        @ConditionalOnBean(TaskStorageJdbcJoinedQueriesImpl.class)
        static class NativeJdbcV2Mode {

        }

        @ConditionalOnBean(TaskStorageQuerydslImpl.class)
        static class QuerydslMode {

        }

        @ConditionalOnBean(TaskStorageJpaImpl.class)
        static class JpaMode {

        }
    }
}
