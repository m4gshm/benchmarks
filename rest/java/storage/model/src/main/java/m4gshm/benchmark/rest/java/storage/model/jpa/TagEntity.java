package m4gshm.benchmark.rest.java.storage.model.jpa;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "tag")
@IdClass(TagEntity.ID.class)
public class TagEntity {
    @Id
    String taskId;
    @Id
    String tag;

    public record ID(String taskId, String tag) {

    }
}
