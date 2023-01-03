package m4gshm.benchmark.rest.java.storage.model.jpa;


import lombok.*;


import java.io.Serializable;

@javax.persistence.Entity
@jakarta.persistence.Entity
@ToString
@javax.persistence.Table(name = "task_tag")
@jakarta.persistence.Table(name = "task_tag")
@javax.persistence.IdClass(TagEntity.ID.class)
@jakarta.persistence.IdClass(TagEntity.ID.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TagEntity {
    @javax.persistence.Id
    @jakarta.persistence.Id
    @javax.persistence.Column(name = "task_id")
    @jakarta.persistence.Column(name = "task_id")
    String taskId;
    @javax.persistence.Id
    @jakarta.persistence.Id
    String tag;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class ID implements Serializable {
        private String taskId;
        private String tag;
    }
}
