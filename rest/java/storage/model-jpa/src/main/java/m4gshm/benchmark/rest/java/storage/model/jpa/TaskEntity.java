package m4gshm.benchmark.rest.java.storage.model.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;

import java.time.LocalDateTime;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;
import static m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity.TABLE_NAME_TASK;

@Builder
@JsonInclude(NON_NULL)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = PRIVATE)
@Access(AccessType.FIELD)
@Table(name = TABLE_NAME_TASK)
public class TaskEntity implements Task<LocalDateTime>, IdAware<String>, WithId<TaskEntity, String> {
    public static final String TABLE_NAME_TASK = "task";
    @Getter
    @Setter
    @Id
    String id;
    @Getter
    @Setter
    String text;
    @Getter
    @Setter
    LocalDateTime deadline;

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = ALL, fetch = EAGER)
    @JoinColumn(name = "id")
    Set<TagEntity> tagEntities;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public TaskEntity withId(String id) {
        this.id = id;
        var oldTags = this.tagEntities;
        if (oldTags != null) for (var tagEntity : oldTags) {
            tagEntity.taskId = id;
        }
        return this;
    }

    @Override
    @Transient
    public Set<String> getTags() {
        return requireNonNullElse(tagEntities, Set.<TagEntity>of()).stream().map(t -> t.tag).collect(toSet());
    }

    public void setTags(Set<String> tags) {
        var newTags = tags.stream().map(t -> new TagEntity(this.id, t)).collect(toSet());
        var oldTags = this.tagEntities;
        if (oldTags != null) {
            oldTags.addAll(newTags);
        } else {
            this.tagEntities = newTags;
        }
    }
}
