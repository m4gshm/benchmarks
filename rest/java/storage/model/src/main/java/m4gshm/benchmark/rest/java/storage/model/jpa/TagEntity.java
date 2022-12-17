package m4gshm.benchmark.rest.java.storage.model.jpa;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Table(name = "task_tag")
@IdClass(TagEntity.ID.class)
@NoArgsConstructor
@AllArgsConstructor
public class TagEntity {
    @Id
    String taskId;
    @Id
    String tag;

    @Data
    public static final class ID {
        private String taskId;
        private String tag;
    }
}
