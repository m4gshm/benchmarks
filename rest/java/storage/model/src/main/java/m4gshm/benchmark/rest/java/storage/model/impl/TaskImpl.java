package m4gshm.benchmark.rest.java.storage.model.impl;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;
import meta.Meta;
import meta.Meta.Extend;
import meta.Meta.Extend.Opt;
import meta.customizer.JpaColumns;

import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static meta.customizer.JpaColumns.OPT_CLASS_NAME;

@Builder(toBuilder = true)
@Meta(customizers = @Extend(value = JpaColumns.class, opts = @Opt(key = OPT_CLASS_NAME, value = "TaskColumn")))
public record TaskImpl(
        @Id @Getter @With String id,
        @Getter String text,
        @Getter LocalDateTime deadline,
       @Transient @Getter Set<String> tags
) implements Task<LocalDateTime>, IdAware<String>, WithId<TaskImpl, String> {
    public static final String TABLE_NAME_TASK = "task";
    public static final String TABLE_NAME_TASK_TAG = "task_tag";

    public static TaskImpl initId(TaskImpl task) {
        var id = task.getId();
        return id == null ? task.toBuilder().id(UUID.randomUUID().toString()).build() : task;
    }
}
