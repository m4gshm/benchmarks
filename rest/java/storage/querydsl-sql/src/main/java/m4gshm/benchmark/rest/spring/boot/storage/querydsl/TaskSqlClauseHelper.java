package m4gshm.benchmark.rest.spring.boot.storage.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLMergeClause;
import com.querydsl.sql.types.Null;
import lombok.experimental.UtilityClass;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.QTask;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.QTaskTag;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.TaskDto;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.TaskTagDto;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.List;

import static com.querydsl.core.types.ConstantImpl.create;
import static java.util.stream.Collectors.toSet;
import static m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.PostgresSQLSerializer.newPostgresSQLSerializer;

@UtilityClass
public class TaskSqlClauseHelper {

    @NotNull
    public static TaskImpl newTaskEntity(TaskDto taskDto, List<TaskTagDto> tags) {
        return TaskImpl.builder()
                .id(taskDto.getId())
                .text(taskDto.getText())
                .deadline(taskDto.getDeadline())
                .tags(tags.stream().map(TaskTagDto::getTag).collect(toSet()))
                .build();
    }

    @NotNull
    private static SQLQuery<Object> newSelectClause(Connection connection, Configuration configuration, boolean indexedParams) {
        return new SQLQuery<>(connection, configuration) {
            @Override
            protected SQLSerializer createSerializer() {
                var serializer = newPostgresSQLSerializer(configuration, indexedParams, false);
                serializer.setUseLiterals(useLiterals);
                return serializer;
            }
        };
    }

    @NotNull
    public static SQLInsertClause newInsertClause(Connection connection, Configuration configuration, RelationalPath<?> entity, boolean indexedParams) {
        return new SQLInsertClause(connection, configuration, entity) {
            @Override
            protected SQLSerializer createSerializer() {
                var serializer = newPostgresSQLSerializer(configuration, indexedParams, true);
                serializer.setUseLiterals(useLiterals);
                return serializer;
            }
        };
    }

    @NotNull
    public static SQLDeleteClause newDeleteClause(Connection connection, Configuration configuration, RelationalPath<?> entity, boolean indexedParams) {

        return new SQLDeleteClause(connection, configuration, entity) {
            @Override
            protected SQLSerializer createSerializer() {
                var serializer = newPostgresSQLSerializer(configuration, indexedParams, true);
                serializer.setUseLiterals(useLiterals);
                return serializer;
            }
        };
    }

    @NotNull
    public static SQLMergeClause newSQLMergeClause(Connection connection, Configuration configuration, boolean indexedParams) {
        return new SQLMergeClause(connection, configuration, QTask.task) {

            @Override
            protected SQLSerializer createSerializer() {
                var serializer = newPostgresSQLSerializer(configuration, indexedParams, true);
                serializer.setUseLiterals(useLiterals);
                return serializer;
            }

            @Override
            public <T> SQLMergeClause set(Path<T> path, @Nullable T value) {
                var clause = super.set(path, value);
                overwriteLastNull(path, values);
                return clause;
            }

            @Override
            public <T> SQLMergeClause setNull(Path<T> path) {
                var clause = super.setNull(path);
                overwriteLastNull(path, values);
                return clause;
            }
        };
    }

    private static <T> void overwriteLastNull(Path<T> path, List<Expression<?>> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        int lastIndex = values.size() - 1;
        if (values.get(lastIndex) == Null.CONSTANT) {
            values.set(lastIndex, create(new TypedNull(path.getType())));
        }
    }

    @NotNull
    public static SQLQuery<TaskDto> selectTaskClause(Connection connection, Configuration configuration, boolean indexedParams) {
        return newSelectClause(connection, configuration, indexedParams)
                .select(Projections.bean(TaskDto.class, QTask.task.id, QTask.task.text, QTask.task.deadline))
                .from(QTask.task);
    }

    @NotNull
    public static SQLQuery<TaskTagDto> selectTagClause(Connection connection, Configuration configuration, boolean indexedParams) {
        return newSelectClause(connection, configuration, indexedParams)
                .select(Projections.bean(TaskTagDto.class, QTaskTag.taskTag.taskId, QTaskTag.taskTag.tag))
                .from(QTaskTag.taskTag);
    }

    @NotNull
    public static SQLQuery<TaskDto> selectTaskByIdClause(Connection connection, Configuration configuration, boolean indexedParams, String taskId) {
        return selectTaskClause(connection, configuration, indexedParams).where(QTask.task.id.eq(taskId));
    }

    @NotNull
    public static SQLDeleteClause deleteTagByTaskIdClause(Connection connection, Configuration configuration, boolean indexedParams, String taskId) {
        QTaskTag taskTag = QTaskTag.taskTag;
        return newDeleteClause(connection, configuration, taskTag, indexedParams).where(taskTag.taskId.eq(taskId));
    }

    @NotNull
    public static SQLDeleteClause deleteTaskByIdClause(Connection connection, Configuration configuration, boolean indexedParams, String id) {
        QTask task = QTask.task;
        return newDeleteClause(connection, configuration, task, indexedParams).where(task.id.eq(id));
    }

    @NotNull
    public static SQLMergeClause upsertTaskClause(
            Connection connection, Configuration configuration, boolean indexedParams, TaskImpl entity, String id) {
        return newSQLMergeClause(connection, configuration, indexedParams)
                .keys(QTask.task.id)
                .set(QTask.task.id, id)
                .set(QTask.task.deadline, entity.getDeadline())
                .set(QTask.task.text, entity.getText());
    }

}
