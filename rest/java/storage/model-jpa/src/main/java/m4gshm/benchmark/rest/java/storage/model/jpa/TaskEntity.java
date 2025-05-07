package m4gshm.benchmark.rest.java.storage.model.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.FieldDefaults;
import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;
import meta.Meta;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;
import static m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity.TABLE_NAME_TASK;

@Builder
@JsonInclude(NON_NULL)
@jakarta.persistence.Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@FieldDefaults(level = PRIVATE)
@jakarta.persistence.Access(jakarta.persistence.AccessType.FIELD)
@jakarta.persistence.Table(name = TABLE_NAME_TASK)
@Getter
@Setter
@Meta
public class TaskEntity implements Task, WithId<TaskEntity, String> {
    public static final String TABLE_NAME_TASK = "task";
    @Id
    @ToString.Include
    private String id;
    @ToString.Include
    String text;
    @ToString.Include
    LocalDateTime deadline;
    @JsonIgnore
    @OneToMany(mappedBy = "task", cascade = ALL, fetch = EAGER)
    private Set<TagEntity> tagEntities;

    public static TaskEntity initId(TaskEntity task) {
        var id = task.getId();
        return id == null ? task.withId(UUID.randomUUID().toString()) : task;
    }

    @Override
    public TaskEntity withId(String id) {
        setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
//        setTagsIs(id);
    }

//    private void setTagsIs(String id) {
//        var oldTags = this.tagEntities;
//        if (oldTags != null) for (var tagEntity : oldTags) {
//            tagEntity.task = this;
//        }
//    }

    @Override
    @Transient
    @JsonInclude(NON_EMPTY)
    public Set<String> getTags() {
        var tagEntities = this.tagEntities;
        return tagEntities != null ? tagEntities.stream().map(t -> t.tag).collect(toSet()) : null;
    }

    public void setTags(Set<String> tags) {
        if (tags == null) {
            this.tagEntities = null;
        } else {
            var newTags = tags.stream().map(t -> new TagEntity(this, t)).collect(toSet());
            var oldTags = this.tagEntities;
            if (oldTags != null) {
                oldTags.addAll(newTags);
            } else {
                this.tagEntities = newTags;
            }
        }
    }
}
