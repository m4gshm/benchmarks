package m4gshm.benchmark.rest.spring.boot.storage.querydsl.jdbc;

import com.querydsl.core.QueryFlag;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import lombok.SneakyThrows;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.TaskSqlClauseHelper;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.QTaskTag;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.TaskDto;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.TaskTagDto;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static m4gshm.benchmark.rest.spring.boot.storage.querydsl.TaskSqlClauseHelper.*;

public class TaskQuerydslRepositoryImpl implements Storage<TaskImpl, String> {

    private final Configuration configuration;
    private final DataSource dataSource;

    public TaskQuerydslRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.configuration = new Configuration(new PostgreSQLTemplates() {{
            setNativeMerge(true);
        }});
    }


    @NotNull
    @SneakyThrows
    private Connection getConnection() {
        var connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    private List<TaskTagDto> selectTags(Connection connection, String taskId) {
        return selectTagClause(connection, configuration, false).where(QTaskTag.taskTag.taskId.eq(taskId)).fetch();
    }

    private List<TaskTagDto> selectTagsIn(Connection connection, Collection<String> taskTags) {
        return selectTagClause(connection, configuration, false).where(QTaskTag.taskTag.taskId.in(taskTags)).fetch();
    }

    @Override
    @SneakyThrows
    public TaskImpl get(String id) {
        try (var connection = getConnection()) {
            return ofNullable(TaskSqlClauseHelper.selectTaskByIdClause(connection, configuration, false, id).fetchFirst())
                    .map(dto -> newTaskEntity(dto, selectTags(connection, id)))
                    .orElse(null);
        }
    }

    @Override
    @SneakyThrows
    public List<TaskImpl> getAll() {
        try (var connection = getConnection()) {
            var taskDtos = selectTaskClause(connection, configuration, false).fetch();
            if (!taskDtos.isEmpty()) {
                var ids = taskDtos.stream().map(TaskDto::getId).collect(toSet());
                var tagsPerTaskId = selectTagsIn(connection, ids).stream().collect(groupingBy(TaskTagDto::getTaskId));
                return taskDtos.stream().map(dto -> newTaskEntity(dto, tagsPerTaskId.get(dto.getId()))).toList();
            }
            return List.of();
        }
    }

    @Override
    @SneakyThrows
    public TaskImpl store(TaskImpl entity) {
        try (var connection = getConnection()) {
            var id = entity.getId();
            var upsertTasks = upsertTaskClause(connection, configuration, false, entity, id).execute();

            var tags = requireNonNullElse(entity.getTags(), List.<String>of());

            var tagsCondition = QTaskTag.taskTag.taskId.eq(id);
            tagsCondition = tags.isEmpty() ? tagsCondition : tagsCondition.and(QTaskTag.taskTag.tag.notIn(tags));
            var deletedTags = newDeleteClause(connection, configuration, QTaskTag.taskTag, false)
                    .where(tagsCondition).execute();

            if (!tags.isEmpty()) {
                var insertTagClause = newInsertClause(connection, configuration, QTaskTag.taskTag, false);
                for (var tag : tags) {
                    insertTagClause
                            .set(QTaskTag.taskTag.tag, tag)
                            .set(QTaskTag.taskTag.taskId, id)
                            .addFlag(QueryFlag.Position.END, " on conflict do nothing")
                            .addBatch();
                }
                long insertedTags = insertTagClause.execute();
            }
            connection.commit();
            return entity;
        }
    }

    @Override
    @SneakyThrows
    public boolean delete(String id) {
        try (var connection = getConnection()) {
            var deletedTags = deleteTagByTaskIdClause(connection, configuration, false, id).execute();
            var deletedTasks = deleteTaskByIdClause(connection, configuration, false, id).execute();
            connection.commit();
            return deletedTasks > 0;
        }
    }

}
