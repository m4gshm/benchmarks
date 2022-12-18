package m4gshm.benchmark.rest.spring.boot.storage.r2dbc.model;

import lombok.Builder;
import lombok.Getter;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;

import java.time.LocalDateTime;
import java.util.Set;


@Builder(toBuilder = true)
public record TaskEntity(
        @Getter String id, @Getter String text, @Getter LocalDateTime deadline, @Getter Set<String> tags
) implements Task<LocalDateTime>, WithId<TaskEntity, String> {

    public static final String TABLE_NAME_TASK = "task";
    public static final String TABLE_NAME_TASK_TAG = "task_TAG";
    public static final String TASK_COLUMN_ID = "id";
    public static final String TASK_COLUMN_TEXT = "text";
    public static final String TASK_COLUMN_DEADLINE = "deadline";
    public static final String TASK_TAG_COLUMN_TASK_ID = "task_id";
    public static final String TASK_TAG_COLUMN_TAG = "tag";

    @Override
    public TaskEntity withId(String id) {
        return this.toBuilder().id(id).build();
    }
}
