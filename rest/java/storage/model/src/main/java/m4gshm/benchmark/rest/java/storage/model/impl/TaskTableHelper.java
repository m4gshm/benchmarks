package m4gshm.benchmark.rest.java.storage.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl.TaskImplBuilder;
import m4gshm.benchmark.rest.java.storage.sql.Column;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static lombok.AccessLevel.PRIVATE;

@UtilityClass
public class TaskTableHelper {
    public static final String TABLE_NAME_TASK = "task";
    public static final String TABLE_NAME_TASK_TAG = "task_tag";

    @RequiredArgsConstructor(access = PRIVATE)
    public final class TaskColumn<T> implements Column {
        public static final TaskColumn<String> ID = new TaskColumn<>("id", true, String.class, TaskImplBuilder::id, TaskImpl::id);
        public static final TaskColumn<String> TEXT = new TaskColumn<>("text", false, String.class, TaskImplBuilder::text, TaskImpl::text);
        public static final TaskColumn<LocalDateTime> DEADLINE = new TaskColumn<>("deadline", false, LocalDateTime.class, TaskImplBuilder::deadline, TaskImpl::deadline);

        private static final List<TaskColumn<?>> values = List.of(ID, TEXT, DEADLINE);

        @Getter
        private final String name;
        @Getter
        private final boolean pk;
        @Getter
        private final Class<T> type;
        private final BiFunction<TaskImplBuilder, T, TaskImplBuilder> builderField;
        private final Function<TaskImpl, T> getter;

        public static List<TaskColumn<?>> values() {
            return TaskColumn.values;
        }

        public void set(TaskImplBuilder builder, T value) {
            builderField.apply(builder, value);
        }

        public T get(TaskImpl bean) {
            return getter.apply(bean);
        }
    }

    @Getter
    @RequiredArgsConstructor(access = PRIVATE)
    public final class TaskTagColumn<T> implements Column {
        public static final TaskTagColumn<String> ID = new TaskTagColumn<>("task_id", true, String.class);
        public static final TaskTagColumn<String> TAG = new TaskTagColumn<>("tag", false, String.class);
        private static final List<TaskTagColumn<?>> values = List.of(ID, TAG);
        private final String name;
        private final boolean pk;
        private final Class<T> type;

        public static List<TaskTagColumn<?>> values() {
            return TaskTagColumn.values;
        }
    }

}
