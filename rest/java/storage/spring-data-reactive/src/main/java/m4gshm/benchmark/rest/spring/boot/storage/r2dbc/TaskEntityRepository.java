package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntityPersistable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;


public interface TaskEntityRepository<T extends TaskEntityPersistable> extends R2dbcRepository<T, String> {

}
