package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import m4gshm.benchmark.rest.spring.boot.storage.jdbc.TaskStorageJdbcImpl;

import javax.sql.DataSource;


@Dependent
//@LookupIfProperty(name = TaskStorageJdbcConfiguration.QUARKUS_STORAGE_JDBC_ACTIVE, stringValue = "true", lookupIfMissing = true)
public class TaskStorageJdbcConfiguration {

    public static final String QUARKUS_STORAGE_JDBC_ACTIVE = "quarkus.storage.jdbc.active";

    @Inject
    DataSource dataSource;

    @Produces
    @ApplicationScoped
    public TaskStorageJdbcImpl taskStorage() {
        return new TaskStorageJdbcImpl(dataSource);
    }

}
