package m4gshm.benchmark.rest.quarkus.storage.db;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.quarkus.BuildTimeProperties;

import javax.enterprise.context.ApplicationScoped;

import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.STORAGE;
import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.STORAGE_VAL_DB;

@ApplicationScoped
@IfBuildProperty(name = STORAGE, stringValue = STORAGE_VAL_DB, enableIfMissing = true)
public class TaskPanacheRepository implements PanacheRepositoryBase<TaskEntity, String> {
}
