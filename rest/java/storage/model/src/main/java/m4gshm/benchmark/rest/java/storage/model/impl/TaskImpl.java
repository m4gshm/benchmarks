package m4gshm.benchmark.rest.java.storage.model.impl;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;
import meta.Meta;
import meta.Meta.Extend;
import meta.Meta.Extend.Opt;
import meta.jpa.customizer.JpaColumns;

import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Set;

import static meta.jpa.customizer.JpaColumns.OPT_CLASS_NAME;
import static meta.jpa.customizer.JpaColumns.OPT_GENERATED_COLUMN_NAME_POST_PROCESS;

@Builder
@Meta(customizers = @Extend(value = JpaColumns.class, opts = {
        @Opt(key = OPT_CLASS_NAME, value = "TaskColumn"),
        @Opt(key = OPT_GENERATED_COLUMN_NAME_POST_PROCESS, value = "toLowerCase"),
}))
public record TaskImpl(
        @Id @Getter @With String id,
        @Getter String text,
        @Getter LocalDateTime deadline,
        @With @Transient @Getter Set<String> tags
) implements Task, WithId<TaskImpl, String> {
    public static final String TABLE_NAME_TASK = "task";
    public static final String TABLE_NAME_TASK_TAG = "task_tag";
}
