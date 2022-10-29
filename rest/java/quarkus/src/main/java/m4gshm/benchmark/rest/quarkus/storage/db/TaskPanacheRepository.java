package m4gshm.benchmark.rest.quarkus.storage.db;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;

import javax.enterprise.context.ApplicationScoped;

import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.STORAGE;
import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.STORAGE_VAL_DB;

//@ApplicationScoped
//@IfBuildProperty(name = STORAGE, stringValue = STORAGE_VAL_DB)
public class TaskPanacheRepository implements PanacheRepositoryBase<TaskEntity, String> {
}
