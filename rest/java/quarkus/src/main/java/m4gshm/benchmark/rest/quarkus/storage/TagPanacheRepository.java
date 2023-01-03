package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import m4gshm.benchmark.rest.java.storage.model.jpa.TagEntity;

public class TagPanacheRepository implements PanacheRepositoryBase<TagEntity, TagEntity.ID> {
}
