package m4gshm.benchmark.rest.spring.boot.storage.jpa;

import m4gshm.benchmark.rest.java.storage.model.jpa.TagEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;


public interface TagEntityRepository extends CrudRepository<TagEntity, TagEntity.ID> {


    void deleteAllByTaskIdAndTagNotIn(String taskId, Collection<String> tags);
    void deleteAllByTaskId(String taskId);

}
