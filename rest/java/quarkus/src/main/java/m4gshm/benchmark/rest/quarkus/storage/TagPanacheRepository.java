package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import m4gshm.benchmark.rest.java.storage.model.jpa.TagEntity;

import java.util.Collection;

public class TagPanacheRepository implements PanacheRepositoryBase<TagEntity, TagEntity.ID> {

    public long deleteByTaskIdExcept(String taskId, Collection<String> newTags) {
        return delete("task.id = ?1 and not tag in (?2) ", taskId, newTags);
    }

    public long deleteByTaskId(String taskId) {
        return delete("task.id = ?1 ", taskId);
    }
}
