package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import m4gshm.benchmark.rest.java.storage.model.jpa.TagEntity;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;

public class TagPanacheRepository implements PanacheRepositoryBase<TagEntity, TagEntity.ID> {

    public long deleteByTask(TaskEntity entity) {
        return delete("task = ?1", entity);
    }
}
