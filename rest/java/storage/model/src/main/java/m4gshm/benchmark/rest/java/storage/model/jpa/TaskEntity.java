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

@Builder
@Data
@JsonInclude(NON_NULL)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Table(name = "task")
@Access(AccessType.PROPERTY)
public class TaskEntity implements Task<LocalDateTime>, IdAware<String>, WithId<TaskEntity, String> {
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

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

}
