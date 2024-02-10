package m4gshm.benchmark.rest.spring.boot.storage.jpa;

import lombok.experimental.UtilityClass;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;

@UtilityClass
public class TaskEntityConvertHelper {
    public static TaskImpl toImpl(TaskEntity taskEntity) {
        return TaskImpl.builder()
                .id(taskEntity.getId())
                .deadline(taskEntity.getDeadline())
                .text(taskEntity.getText())
                .tags(taskEntity.getTags())
                .build();
    }

    public static TaskEntity toJpa(TaskImpl entity) {
        var task = TaskEntity.builder()
                .id(entity.getId())
                .text(entity.getText())
                .deadline(entity.getDeadline())
                .build();
        task.setTags(entity.getTags());
        return task;
    }
}
