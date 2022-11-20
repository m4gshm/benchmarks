package m4gshm.benchmark.rest.java.storage.model.jpa;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

import static lombok.AccessLevel.NONE;
import static m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity.TABLE_NAME_TASK;

@org.springframework.data.relational.core.mapping.Table(name = TABLE_NAME_TASK)
public class TaskEntityPersistable extends TaskEntity implements Persistable<String> {


    private boolean isNew;

    public static String initId(TaskEntityPersistable task) {
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
    public TaskEntity asNew() {
        isNew = true;
        return this;
    }

    @Transient
    @Override
    public boolean isNew() {
        return isNew;

    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    @org.springframework.data.annotation.Id
    @Override
    public LocalDateTime getDeadline() {
        return super.getDeadline();
    }
}
