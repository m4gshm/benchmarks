package m4gshm.benchmark.rest.spring.boot.storage.jpa;

import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.springframework.data.repository.CrudRepository;


public interface TaskEntityRepository extends CrudRepository<TaskEntity<?>, String> {

}
