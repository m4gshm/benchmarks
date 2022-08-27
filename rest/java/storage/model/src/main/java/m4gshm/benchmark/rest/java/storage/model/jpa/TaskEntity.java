package m4gshm.benchmark.rest.java.storage.model.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.Task;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.OffsetDateTime;
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
public class TaskEntity implements Task<OffsetDateTime>, IdAware<String> {

    @Id
    String id;
    String text;
    //    List<String> tags;
    OffsetDateTime deadline;

    public static String initId(TaskEntity task) {
        var id = task.getId();
        if (id == null || id.trim().isEmpty()) {
            var newId = UUID.randomUUID().toString();
            task.setId(newId);
            return newId;
        }
        return null;
    }

}
