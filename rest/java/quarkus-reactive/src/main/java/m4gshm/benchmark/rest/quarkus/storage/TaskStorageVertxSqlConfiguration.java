package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.arc.lookup.LookupIfProperty;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import m4gshm.benchmark.rest.java.storage.MutinyStorage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;

import static m4gshm.benchmark.rest.quarkus.storage.TaskStorageVertxSqlConfiguration.QUARKUS_VERTX_SQL_ACTIVE;


@Dependent
@LookupIfProperty(name = QUARKUS_VERTX_SQL_ACTIVE, stringValue = "true", lookupIfMissing = true)
public class TaskStorageVertxSqlConfiguration {

    public static final String QUARKUS_VERTX_SQL_ACTIVE = "quarkus.vertx-sql.active";

    @Produces
    @ApplicationScoped
    public MutinyStorage<TaskImpl, String> taskStorage(Pool connection) {
        return new TaskStorageVertxSqlImpl(connection);
    }

}
