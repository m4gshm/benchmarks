package m4gshm.benchmark.rest.java.storage.model.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;
import static m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity.TABLE_NAME_TASK;

@Builder
@JsonInclude(NON_NULL)
@javax.persistence.Entity
@jakarta.persistence.Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@FieldDefaults(level = PRIVATE)
@javax.persistence.Access(javax.persistence.AccessType.FIELD)
@jakarta.persistence.Access(jakarta.persistence.AccessType.FIELD)
@javax.persistence.Table(name = TABLE_NAME_TASK)
@jakarta.persistence.Table(name = TABLE_NAME_TASK)
@Getter
@Setter
@Meta
public class TaskEntity implements Task<LocalDateTime>, IdAware<String>, WithId<TaskEntity, String> {
    public static final String TABLE_NAME_TASK = "task";
    @javax.persistence.Id
    @jakarta.persistence.Id
    @ToString.Include
    String id;
    @ToString.Include
    String text;
    @ToString.Include
    LocalDateTime deadline;
    @JsonIgnore
    @javax.persistence.OneToMany(mappedBy = "task", cascade = javax.persistence.CascadeType.ALL, fetch = javax.persistence.FetchType.EAGER)
    @jakarta.persistence.OneToMany(mappedBy = "task", cascade = jakarta.persistence.CascadeType.ALL, fetch = jakarta.persistence.FetchType.EAGER)
    Set<TagEntity> tagEntities;

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

    private void setTagsIs(String id) {
        var oldTags = this.tagEntities;
        if (oldTags != null) for (var tagEntity : oldTags) {
            tagEntity.task = this;
        }
    }

    @Override
    @javax.persistence.Transient
    @jakarta.persistence.Transient
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
