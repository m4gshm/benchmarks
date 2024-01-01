package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskTableHelper.TaskTagColumn;
import m4gshm.benchmark.rest.java.storage.sql.SqlUtils;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskTableHelper.TABLE_NAME_TASK;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskTableHelper.TABLE_NAME_TASK_TAG;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskTableHelper.TaskColumn;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.ModifyDataSqlParts.newModifyDataSqlParts;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.POSTGRES_PLACEHOLDER;
import static reactor.core.publisher.Mono.from;

@RequiredArgsConstructor
public class TaskR2dbcRepositoryImpl implements TaskReactiveRepository<TaskImpl> {

    public static final String SQL_TASK_SELECT_ALL = SqlUtils.selectAll(TABLE_NAME_TASK, TaskColumn.values());
    private static final IntFunction<String> PLACEHOLDER = POSTGRES_PLACEHOLDER;
    public static final String SQL_TASK_SELECT_BY_ID = SqlUtils.selectBy(TABLE_NAME_TASK, TaskColumn.values(), TaskColumn.ID, PLACEHOLDER);
    public static final String SQL_TASK_UPSERT = SqlUtils.upsert(TABLE_NAME_TASK, newModifyDataSqlParts(TaskColumn.values(), PLACEHOLDER));
    public static final String SQL_TASK_DELETE_BY_ID = SqlUtils.deleteBy(TABLE_NAME_TASK, TaskColumn.ID, PLACEHOLDER);
    public static final String SQL_TASK_TAG_SELECT_BY_TASK_ID = SqlUtils.selectBy(
            TABLE_NAME_TASK_TAG, TaskTagColumn.values(), TaskTagColumn.ID, PLACEHOLDER
    );
    public static final String SQL_TASK_TAG_SELECT_BY_TASK_IDS = SqlUtils.selectByAny(
            TABLE_NAME_TASK_TAG, TaskTagColumn.values(), TaskTagColumn.ID, PLACEHOLDER
    );
    public static final String SQL_TASK_TAG_INSERT = SqlUtils.insert(TABLE_NAME_TASK_TAG,
            newModifyDataSqlParts(TaskTagColumn.values(), PLACEHOLDER)) + "ON CONFLICT DO NOTHING";
    public static final String SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID = SqlUtils.deleteBy(
            TABLE_NAME_TASK_TAG, TaskTagColumn.ID, PLACEHOLDER
    ) + " AND NOT " + TaskTagColumn.TAG.getName() + "=ANY($2)";
    public static final String SQL_TASK_TAG_DELETE_BY_TASK_ID = SqlUtils.deleteBy(TABLE_NAME_TASK_TAG, TaskTagColumn.ID, PLACEHOLDER);

    private final ConnectionFactory connectionFactory;

    @NotNull
    private static Statement bind(Statement statement, String name, Object value, Class<?> clazz) {
        return value != null ? statement.bind(name, value) : statement.bindNull(name, clazz);
    }

    @NotNull
    private static Publisher<TaskImpl> mapResultToTaskPublisher(Result result) {
        return result.map((row, rowMetadata) -> {
            var builder = TaskImpl.builder();
            for (TaskColumn column : TaskColumn.values()) {
                column.set(builder, row.get(column.getName(), column.getType()));
            }
            return builder.build();
        });
    }

    @NotNull
    private static Publisher<Entry<String, String>> mapResultToTaskTagPublisher(Result result) {
        return result.map((row, rowMetadata) -> {
            var taskId = row.get(TaskTagColumn.ID.getName(), String.class);
            var tag = row.get(TaskTagColumn.TAG.getName(), String.class);
            return entry(taskId, tag);
        });
    }

    @NotNull
    private static Mono<Long> upsert(Connection connection, TaskImpl entity) {
        var statement = connection.createStatement(SQL_TASK_UPSERT);
        var columns = TaskColumn.values();
        var num = 0;
        for (var column : columns) {
            num++;
            var val = column.get(entity);
            bind(statement, "$" + num, val, column.getType());
        }
        return Flux.from(statement.execute()).flatMap(Result::getRowsUpdated).next();
    }

    @NotNull
    private static Mono<Long> deleteUnusedTags(Connection connection, String taskId, Set<String> tags) {
        return tags == null || tags.isEmpty() ? Mono.empty() : Mono.defer(() -> {
            var statement = connection.createStatement(SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID);
            bind(statement, "$1", taskId, String.class);
            bind(statement, "$2", tags.toArray(new String[0]), String[].class);
            return Flux.from(statement.execute()).flatMap(Result::getRowsUpdated).next();
        });
    }

    @NotNull
    private static Mono<Long> insertTags(Connection connection, String taskId, Set<String> tags) {
        return tags == null || tags.isEmpty() ? Mono.empty() : Mono.defer(() -> {
            var statement = connection.createStatement(SQL_TASK_TAG_INSERT);
            for (var iterator = tags.iterator(); iterator.hasNext(); ) {
                var tag = iterator.next();
                bind(statement, "$1", taskId, String.class);
                bind(statement, "$2", tag, String.class);
                if (iterator.hasNext()) {
                    statement.add();
                }
            }
            return Flux.from(statement.execute()).flatMap(Result::getRowsUpdated).next();
        });
    }

    @NotNull
    private static Mono<Set<String>> getTaskTags(Connection connection, String id) {
        return Flux.from(bind(connection.createStatement(SQL_TASK_TAG_SELECT_BY_TASK_ID), "$1", id, String.class)
                        .execute())
                .flatMap(result -> result.map((row, rowMetadata) -> row.get(TaskTagColumn.TAG.getName(), String.class)))
                .collect(toSet());
    }

    @NotNull
    private static Mono<Map<String, Set<String>>> getTasksTags(Connection connection, String[] ids) {
        return ids == null || ids.length == 0
                ? Mono.empty()
                : Flux.from(bind(connection.createStatement(SQL_TASK_TAG_SELECT_BY_TASK_IDS), "$1", ids, String[].class).execute())
                .flatMap(TaskR2dbcRepositoryImpl::mapResultToTaskTagPublisher)
                .collect(groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, toCollection(toLinkedHashSet()))));
    }

    @NotNull
    private static <T> Supplier<Set<T>> toLinkedHashSet() {
        return LinkedHashSet::new;
    }

    @NotNull
    private static Mono<TaskImpl> getTaskEntity(Connection connection, String id) {
        return Flux.from(bind(connection.createStatement(SQL_TASK_SELECT_BY_ID), "$1", id, String.class)
                        .fetchSize(1).execute())
                .flatMap(TaskR2dbcRepositoryImpl::mapResultToTaskPublisher).next();
    }

    @NotNull
    private static String[] getIds(List<TaskImpl> tasks) {
        return tasks != null ? tasks.stream().map(TaskImpl::id).distinct().toArray(String[]::new) : null;
    }

    @Override
    public Flux<TaskImpl> findAll() {
        return connectFlux(false, connection -> Flux.from(connection.createStatement(SQL_TASK_SELECT_ALL).execute())
                .flatMap(TaskR2dbcRepositoryImpl::mapResultToTaskPublisher).collectList()
                .flatMapMany(tasks -> Flux.fromIterable(tasks).zipWith(getTasksTags(connection, getIds(tasks)),
                        (taskEntity, tagsByTaskId) -> {
                            var tags = tagsByTaskId.get(taskEntity.getId());
                            return tags == null || tags.isEmpty() ? taskEntity : taskEntity.toBuilder().tags(tags).build();
                        }))
        );
    }

    @Override
    public Mono<TaskImpl> findById(String id) {
        return connectMono(false, connection -> Mono.zip(
                getTaskEntity(connection, id), getTaskTags(connection, id),
                (taskEntity, strings) -> taskEntity.toBuilder().tags(strings).build()));
    }

    @Override
    public Mono<TaskImpl> save(TaskImpl entity) {
        return transactMono(connection -> Flux.mergeSequential(
                        upsert(connection, entity),
                        deleteUnusedTags(connection, entity.id(), entity.tags()),
                        insertTags(connection, entity.id(), entity.tags()))
                .collect(toList())
                .map(results -> entity));
    }

    @Override
    public Mono<? extends Number> deleteById(String id) {
        return connectMono(true, connection -> {
            var deleteTask = bind(connection.createStatement(SQL_TASK_DELETE_BY_ID), "$1", id, String.class).execute();
            var deleteTaskTags = bind(
                    connection.createStatement(SQL_TASK_TAG_DELETE_BY_TASK_ID), "$1", id, String.class
            ).execute();
            return Flux.mergeSequential(deleteTaskTags, deleteTask).flatMap(Result::getRowsUpdated)
                    .reduce(Long::sum).map(n -> (Number) n);
        });
    }

    @NotNull
    private Mono<? extends Connection> getConnection(boolean autoCommit) {
        var conn = getConnection();
        return autoCommit ? conn.flatMap(c -> from(c.setAutoCommit(true)).thenReturn(c)) : conn;
    }

    @NotNull
    private Mono<? extends Connection> getConnection() {
        return Mono.from(connectionFactory.create());
    }

    @NotNull
    private <T> Mono<T> connectMono(boolean autoCommit, Function<Connection, Mono<T>> routine) {
        return Mono.usingWhen(getConnection(autoCommit), routine, Connection::close);
    }

    @NotNull
    private <T> Mono<T> transactMono(Function<Connection, Mono<T>> routine) {
        return connectMono(false, c -> from(c.beginTransaction())
                .then(routine.apply(c))
                .flatMap(t -> from(c.commitTransaction()).thenReturn(t)));
    }

    @NotNull
    private <T> Flux<T> connectFlux(boolean autoCommit, Function<Connection, Flux<T>> routine) {
        return Flux.usingWhen(getConnection(autoCommit), routine, Connection::close);
    }

}
