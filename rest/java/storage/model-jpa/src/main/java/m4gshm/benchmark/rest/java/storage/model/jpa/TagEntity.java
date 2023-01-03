package m4gshm.benchmark.rest.java.storage.model.jpa;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.FetchType;
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
    @JsonIgnore
    @javax.persistence.Id
    @jakarta.persistence.Id
//    @javax.persistence.Column(name = "task_id")
//    @jakarta.persistence.Column(name = "task_id")
    @javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    TaskEntity task;
    @javax.persistence.Id
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
