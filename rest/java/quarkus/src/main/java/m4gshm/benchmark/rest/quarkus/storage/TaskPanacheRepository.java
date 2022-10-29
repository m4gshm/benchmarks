package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;

public class TaskPanacheRepository implements PanacheRepositoryBase<TaskEntity, String> {
}
