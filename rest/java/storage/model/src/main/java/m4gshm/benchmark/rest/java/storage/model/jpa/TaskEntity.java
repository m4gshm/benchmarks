package m4gshm.benchmark.rest.java.storage.model.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
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
public class TaskEntity implements Task<LocalDateTime>, IdAware<String>, WithId<TaskEntity, String> {
    public static final String TABLE_NAME_TASK = "task";
    @With
    String id;
    String text;
    LocalDateTime deadline;

    public static String initId(TaskEntity task) {
        var id = task.getId();
        if (id == null || id.trim().isEmpty()) {
            var newId = UUID.randomUUID().toString();
            task.setId(newId);
            return newId;
        }
        return null;
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
