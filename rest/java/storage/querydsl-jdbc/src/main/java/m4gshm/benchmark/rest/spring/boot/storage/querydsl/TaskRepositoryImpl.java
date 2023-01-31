package m4gshm.benchmark.rest.spring.boot.storage.querydsl;

import com.querydsl.core.QueryFlag;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import lombok.SneakyThrows;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.PostgresUpsertClause;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.QTask;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.QTaskTag;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.TaskDto;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.TaskTagDto;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.querydsl.core.types.Projections.bean;
import static java.util.Objects.requireNonNullElse;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class TaskRepositoryImpl implements Storage<TaskImpl, String> {

    private final Configuration configuration;
    private final DataSource dataSource;
    private final QTask qTask = QTask.task;
    private final QTaskTag qTaskTag = QTaskTag.taskTag;

    public TaskRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.configuration = new Configuration(new PostgreSQLTemplates() {{
            setNativeMerge(true);
        }});
    }

    @NotNull
    private static Set<String> toStringTags(List<TaskTagDto> tags) {
        return tags.stream().map(TaskTagDto::getTag).collect(toSet());
    }

    @NotNull
    private static TaskImpl newTaskEntity(TaskDto taskDto, List<TaskTagDto> tags) {
        var entityBuilder = TaskImpl.builder();
        entityBuilder.id(taskDto.getId());
        entityBuilder.text(taskDto.getText());
        entityBuilder.tags(toStringTags(tags));
        return entityBuilder.build();
    }

    @NotNull
    @SneakyThrows
    private Connection getConnection() {
        var connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    @NotNull
    private SQLQuery<TaskDto> selectTaskClause(Connection connection) {
        return new SQLQuery<>(connection, configuration)
                .select(bean(TaskDto.class, qTask.id, qTask.text, qTask.deadline))
                .from(qTask);
    }

    @NotNull
    private SQLQuery<TaskTagDto> selectTagClause(Connection connection) {
        return new SQLQuery<>(connection, configuration)
                .select(bean(TaskTagDto.class, qTaskTag.taskId, qTaskTag.tag))
                .from(qTaskTag);
    }

    private List<TaskTagDto> selectTags(Connection connection, String id) {
        return selectTagClause(connection).where(qTaskTag.taskId.eq(id)).fetch();
    }

    private List<TaskTagDto> selectTagsIn(Connection connection, Collection<String> ids) {
        return selectTagClause(connection).where(qTaskTag.taskId.in(ids)).fetch();
    }

    @Override
    @SneakyThrows
    public TaskImpl get(String id) {
        try (var connection = getConnection()) {
            return ofNullable(selectTaskClause(connection).where(qTask.id.eq(id)).fetchFirst())
                    .map(dto -> newTaskEntity(dto, selectTags(connection, id)))
                    .orElse(null);
        }
    }

    @Override
    @SneakyThrows
    public List<TaskImpl> getAll() {
        try (var connection = getConnection()) {
            var taskDtos = selectTaskClause(connection).fetch();
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
            var upsertTasks = new PostgresUpsertClause(connection, configuration, qTask)
                    .keys(qTask.id)
                    .set(qTask.id, id)
                    .set(qTask.deadline, entity.getDeadline())
                    .set(qTask.text, entity.getText()).execute();

            var tags = requireNonNullElse(entity.getTags(), List.<String>of());

            var tagsCondition = qTaskTag.taskId.eq(id);
            tagsCondition = tags.isEmpty() ? tagsCondition : tagsCondition.and(qTaskTag.tag.notIn(tags));
            var deletedTags = new SQLDeleteClause(connection, configuration, qTaskTag)
                    .where(tagsCondition).execute();

            if (!tags.isEmpty()) {
                var insertTagClause = new SQLInsertClause(connection, configuration, qTaskTag);
                for (var tag : tags) {
                    insertTagClause
                            .set(qTaskTag.tag, tag)
                            .set(qTaskTag.taskId, id)
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
            var deletedTags = new SQLDeleteClause(connection, configuration, qTaskTag).where(qTaskTag.taskId.eq(id)).execute();
            var deletedTasks = new SQLDeleteClause(connection, configuration, qTask).where(qTask.id.eq(id)).execute();
            connection.commit();
            return deletedTasks > 0;
        }
    }

}
