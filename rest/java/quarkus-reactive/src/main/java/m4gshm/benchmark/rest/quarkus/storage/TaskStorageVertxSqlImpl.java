package m4gshm.benchmark.rest.quarkus.storage;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import m4gshm.benchmark.rest.java.storage.MutinyStorage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImplMeta.TaskColumn;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskTagImplMeta.TaskTagColumn;
import m4gshm.benchmark.rest.java.storage.model.impl.sql.TaskStorageQuery;
import org.jetbrains.annotations.NotNull;

import java.sql.Array;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.vertx.mutiny.sqlclient.Tuple.tuple;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static m4gshm.benchmark.rest.java.storage.model.impl.sql.TaskStorageConstants.EMPTY_STRINGS;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.POSTGRES_PLACEHOLDER;

@RequiredArgsConstructor
public class TaskStorageVertxSqlImpl implements MutinyStorage<TaskImpl, String> {

    private static final TaskStorageQuery query = new TaskStorageQuery(POSTGRES_PLACEHOLDER, false);
    private final Pool connection;

    @SneakyThrows
    private static TaskImpl newTaskImp(Row row) {
        var builder = TaskImpl.builder();
        for (TaskColumn<?> column : TaskColumn.values()) {
            populate(row, column, builder);
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <T> void populate(Row row, TaskColumn<T> column, TaskImpl.TaskImplBuilder builder) {
        var type = column.type();
        if (LocalDateTime.class.equals(type)) {
            var localDateTime = row.getLocalDateTime(column.name());
            ((TaskColumn<LocalDateTime>) column).apply(builder, localDateTime);
        } else {
            column.apply(builder, (T) row.getValue(column.name()));
        }
    }

    @NotNull
    @SneakyThrows
    private static Map<String, LinkedHashSet<String>> toTaskTagsMap(RowSet<Row> rowSet) {
        return toStream(rowSet).map(row -> {
            var taskId = getColumnValue(row, TaskTagColumn.task_id);
            var tagId = getColumnValue(row, TaskTagColumn.tag);
            return Map.entry(taskId, tagId);
        }).collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toCollection(LinkedHashSet::new))));
    }

    private static <T> T getColumnValue(Row row, TaskTagColumn<T> column) {
        return row.get(column.type(), column.name());
    }

    @SneakyThrows
    private static Uni<Integer> upsert(SqlClient client, TaskImpl entity) {
        var values = query.SQL_TASK_UPSERT_PLACEHOLDERS.stream().map(placeholder -> {
            var column = placeholder.column();
            return (Object)column.get(entity);
        }).toList();
        var query = client.preparedQuery(TaskStorageVertxSqlImpl.query.SQL_TASK_UPSERT).execute(tuple(values));
        return query.map(SqlResult::rowCount);
    }

    @SneakyThrows
    private static Uni<Integer> deleteUnusedTags(SqlConnection connection, String taskId, Set<String> tags) {
        return tags == null || tags.isEmpty() ? Uni.createFrom().item(0)
                : connection.preparedQuery(query.SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID)
                .execute(Tuple.of(taskId, tags.toArray(new String[0])))
                .map(SqlResult::rowCount);
    }

    @SneakyThrows
    private static Uni<Integer> insertTags(SqlConnection client, String taskId, Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Uni.createFrom().item(0);
        }
        var tuples = tags.stream().map(tag -> Tuple.of(taskId, tag)).toList();
        return client.preparedQuery(query.SQL_TASK_TAG_INSERT).executeBatch(tuples).map(SqlResult::rowCount);
    }

    @SneakyThrows
    private static Uni<Set<String>> getTaskTags(Pool connection, String id) {
        return connection.preparedQuery(query.SQL_TASK_TAG_SELECT_BY_TASK_ID).execute(Tuple.of(id)).map(rs -> {
            return toStream(rs).map(row -> getColumnValue(row, TaskTagColumn.tag)).collect(toSet());
        });
    }

    private static @NotNull Stream<Row> toStream(RowSet<Row> rowSet) {
        return stream(spliteratorUnknownSize(rowSet.iterator(), ORDERED), false);
    }

    @SneakyThrows
    private static Uni<Map<String, LinkedHashSet<String>>> getTasksTags(Pool connection, String[] ids) {
        return ids == null || ids.length == 0
                ? Uni.createFrom().item(Map.of())
                : connection.preparedQuery(query.SQL_TASK_TAG_SELECT_BY_TASK_IDS).execute(Tuple.of(ids))
                .map(TaskStorageVertxSqlImpl::toTaskTagsMap).replaceIfNullWith(Map.of());
    }

    @SneakyThrows
    private static Array newPgArray(Connection connection, String[] ids) {
        return connection.createArrayOf("text", ids);
    }

    @SneakyThrows
    private static Uni<TaskImpl> getTask(Pool connection, String id) {
        return getTaskMulti(connection.preparedQuery(query.SQL_TASK_SELECT_BY_ID).execute(Tuple.of(id))).collect().first();
    }

    @NotNull
    private static String[] getIds(List<TaskImpl> tasks) {
        return tasks != null ? tasks.stream().map(TaskImpl::getId).distinct().toArray(String[]::new) : EMPTY_STRINGS;
    }

    private static Multi<TaskImpl> getTaskMulti(Uni<RowSet<Row>> execute) {
        return execute.onItem()
                .transformToMulti(rowSet -> Multi.createFrom().iterable(rowSet)).onItem()
                .transform(TaskStorageVertxSqlImpl::newTaskImp);
    }

    private static Uni<TaskImpl> withTags(Uni<TaskImpl> task, Uni<Set<String>> tags) {
        return Uni.combine().all().unis(task, tags).with((_task, _tags) -> {
            return _tags != null && !_tags.isEmpty() ? _task.toBuilder().tags(_tags).build() : _task;
        });
    }

    @Override
    @SneakyThrows
    public Uni<List<TaskImpl>> getAll() {
        return getTaskMulti(connection.preparedQuery(query.SQL_TASK_SELECT_ALL).execute()).collect().asList().flatMap(tasks -> {
            return getTasksTags(connection, getIds(tasks)).map(tagsByTaskId -> {
                return tasks.stream().map(task -> {
                    return task.toBuilder().tags(tagsByTaskId.get(task.getId())).build();
                }).toList();
            });
        });
    }

    @Override
    @SneakyThrows
    public Uni<TaskImpl> get(String id) {
        return withTags(getTask(connection, id), getTaskTags(connection, id));
    }

    @Override
    @SneakyThrows
    public Uni<TaskImpl> store(String id, TaskImpl entity) {
        var _entity = id != null ? entity.toBuilder().id(id).build() : entity;
        return connection.withTransaction(connection -> {
            return upsert(connection, _entity).call(() -> {
                return deleteUnusedTags(connection, _entity.getId(), _entity.getTags()).chain(() -> {
                    return insertTags(connection, _entity.getId(), _entity.getTags());
                });
            }).map(c -> _entity);
        });
    }

    @Override
    @SneakyThrows
    public Uni<Boolean> delete(String id) {
        var delTags = connection.preparedQuery(query.SQL_TASK_TAG_DELETE_BY_TASK_ID).execute(Tuple.of(id));
        var delTask = connection.preparedQuery(query.SQL_TASK_DELETE_BY_ID).execute(Tuple.of(id));
        return delTags.chain(() -> delTask).map(rs -> {
            return rs.rowCount() > 0;
        });
    }
}
