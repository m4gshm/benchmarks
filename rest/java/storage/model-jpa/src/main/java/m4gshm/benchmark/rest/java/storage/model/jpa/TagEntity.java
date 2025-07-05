package m4gshm.benchmark.rest.java.storage.model.jpa;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;

import static m4gshm.benchmark.rest.java.storage.model.jpa.TagEntity.TASK_TAG_TABLE;

@jakarta.persistence.Entity
@ToString
@jakarta.persistence.Table(name = TASK_TAG_TABLE)
@jakarta.persistence.IdClass(TagEntity.ID.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TagEntity {
    public static final String TASK_TAG_TABLE = "task_tag";
    @JsonIgnore
    @jakarta.persistence.Id
    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    TaskEntity task;
    @jakarta.persistence.Id
    String tag;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class ID implements Serializable {
        private TaskEntity task;
        private String tag;
    }
}
