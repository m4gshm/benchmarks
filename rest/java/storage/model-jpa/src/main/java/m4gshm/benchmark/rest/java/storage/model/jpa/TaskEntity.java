package m4gshm.benchmark.rest.java.storage.model.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;

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
@ToString
@FieldDefaults(level = PRIVATE)
@javax.persistence.Access(javax.persistence.AccessType.FIELD)
@jakarta.persistence.Access(jakarta.persistence.AccessType.FIELD)
@javax.persistence.Table(name = TABLE_NAME_TASK)
@jakarta.persistence.Table(name = TABLE_NAME_TASK)
@Getter
@Setter
public class TaskEntity implements Task<LocalDateTime>, IdAware<String>, WithId<TaskEntity, String> {
    public static final String TABLE_NAME_TASK = "task";
    @javax.persistence.Id
    @jakarta.persistence.Id
    String id;
    String text;
    LocalDateTime deadline;
    @JsonIgnore
    @javax.persistence.OneToMany(/*orphanRemoval = true, cascade = javax.persistence.CascadeType.ALL,fetch = javax.persistence.FetchType.EAGER */)
    @jakarta.persistence.OneToMany(/*orphanRemoval = true, cascade = jakarta.persistence.CascadeType.ALL, fetch = jakarta.persistence.FetchType.EAGER*/)
    @javax.persistence.JoinColumn(name = "task_id", referencedColumnName = "id")
    @jakarta.persistence.JoinColumn(name = "task_id", referencedColumnName = "id")
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
        setTagsIs(id);
    }

    private void setTagsIs(String id) {
        var oldTags = this.tagEntities;
        if (oldTags != null) for (var tagEntity : oldTags) {
            tagEntity.taskId = id;
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
        var newTags = tags.stream().map(t -> new TagEntity(this.id, t)).collect(toSet());
        var oldTags = this.tagEntities;
        if (oldTags != null) {
            oldTags.addAll(newTags);
        } else {
            this.tagEntities = newTags;
        }
    }
}
