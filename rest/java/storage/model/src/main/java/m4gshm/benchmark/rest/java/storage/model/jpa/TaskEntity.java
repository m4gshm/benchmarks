package m4gshm.benchmark.rest.java.storage.model.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PRIVATE;
import static m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity.TABLE_NAME_TASK;

@Builder
@Data
@JsonInclude(NON_NULL)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Access(AccessType.PROPERTY)
@Table(name = TABLE_NAME_TASK)
@org.springframework.data.relational.core.mapping.Table(name = TABLE_NAME_TASK)
public class TaskEntity implements Task<LocalDateTime>, IdAware<String>, WithId<TaskEntity, String>, Persistable<String> {
    public static final String TABLE_NAME_TASK = "task";
    @With
    @org.springframework.data.annotation.Id
    String id;
    String text;
    LocalDateTime deadline;

    @org.springframework.data.annotation.Transient
    @Getter(NONE)
    @Setter(NONE)
    private boolean isNew;

    public static String initId(TaskEntity task) {
        var id = task.getId();
        if (id == null || id.trim().isEmpty()) {
            var newId = UUID.randomUUID().toString();
            task.setId(newId);
            task.isNew = true;
            return newId;
        }
        return null;
    }

    @Transient
    @Override
    public boolean isNew() {
        return isNew;
    }

    @Transient
    public TaskEntity asNew() {
        isNew = true;
        return this;
    }

    @Id
    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
