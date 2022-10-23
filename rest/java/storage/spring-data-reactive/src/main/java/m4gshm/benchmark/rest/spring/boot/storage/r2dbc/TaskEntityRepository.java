package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;


public interface TaskEntityRepository extends R2dbcRepository<TaskEntity, String> {

}
