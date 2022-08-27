package m4gshm.benchmark.rest.java.storage.panache;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;


public class TaskPanacheRepository implements PanacheRepositoryBase<TaskEntity, String> {
}
