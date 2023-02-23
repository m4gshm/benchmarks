package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static java.util.stream.Collectors.*;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskTableHelper.*;
import static reactor.core.publisher.Mono.from;

@RequiredArgsConstructor
public class TaskR2dbcRepositoryImpl implements TaskReactiveRepository<TaskImpl> {

    public static final String SQL_TASK_SELECT_ALL = "SELECT " +
            TASK_COLUMN_ID + "," +
            TASK_COLUMN_TEXT + "," +
            TASK_COLUMN_DEADLINE +
            " FROM " + TABLE_NAME_TASK;
    public static final String SQL_TASK_SELECT_BY_ID = SQL_TASK_SELECT_ALL + " WHERE " + TASK_COLUMN_ID + " = $1";
    public static final String SQL_TASK_UPSERT = "INSERT INTO " +
            TABLE_NAME_TASK +
            "(" +
            TASK_COLUMN_ID + "," +
            TASK_COLUMN_TEXT + "," +
            TASK_COLUMN_DEADLINE +
            ") VALUES ($1,$2,$3) " +
            "ON CONFLICT (" + TASK_COLUMN_ID +
            ") DO UPDATE SET " +
            TASK_COLUMN_TEXT + " = $2," +
            TASK_COLUMN_DEADLINE + " = $3";
    public static final String SQL_TASK_DELETE_BY_ID = "DELETE FROM " + TABLE_NAME_TASK + " WHERE " + TASK_COLUMN_ID + "=$1";

    public static final String SQL_TASK_TAG_SELECT_BY_TASK_ID = "SELECT " + TASK_TAG_COLUMN_TAG + " FROM " +
            TABLE_NAME_TASK_TAG + " WHERE " + TASK_TAG_COLUMN_TASK_ID + "=$1";

    public static final String SQL_TASK_TAG_SELECT_BY_TASK_IDS = "SELECT " + TASK_TAG_COLUMN_TASK_ID + "," +
            TASK_TAG_COLUMN_TAG + " FROM " + TABLE_NAME_TASK_TAG + " WHERE " +
            TASK_TAG_COLUMN_TASK_ID + " = any($1)";

    public static final String SQL_TASK_TAG_INSERT = "INSERT INTO " + TABLE_NAME_TASK_TAG +
            "(" +
            TASK_TAG_COLUMN_TASK_ID + "," +
            TASK_TAG_COLUMN_TAG +
            ") VALUES ($1,$2) ON CONFLICT DO NOTHING";

    public static final String SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID = "DELETE FROM " + TABLE_NAME_TASK_TAG +
            " WHERE " + TASK_TAG_COLUMN_TASK_ID + "=$1 AND NOT " + TASK_TAG_COLUMN_TAG + "=ANY($2)";

    public static final String SQL_TASK_TAG_DELETE_BY_TASK_ID = "DELETE FROM " + TABLE_NAME_TASK_TAG +
            " WHERE " + TASK_TAG_COLUMN_TASK_ID + "=$1";

    private final ConnectionFactory connectionFactory;

    @NotNull
    private static <T> Statement bind(Statement statement, String name, T value, Class<T> clazz) {
        return value != null ? statement.bind(name, value) : statement.bindNull(name, clazz);
    }

    @NotNull
    private static Publisher<TaskImpl> mapResultToTaskPublisher(Result result) {
        return result.map((row, rowMetadata) -> TaskImpl.builder()
                .id(row.get(TASK_COLUMN_ID, String.class))
                .text(row.get(TASK_COLUMN_TEXT, String.class))
                .deadline(row.get(TASK_COLUMN_DEADLINE, LocalDateTime.class))
                .build());
    }

    @NotNull
    private static Publisher<Entry<String, String>> mapResultToTaskTagPublisher(Result result) {
        return result.map((row, rowMetadata) -> {
            var taskId = row.get(0, String.class);
            var tag = row.get(1, String.class);
            return entry(taskId, tag);
        });
    }

    @NotNull
    private static Mono<Long> upsert(Connection connection, TaskImpl entity) {
        var statement = connection.createStatement(SQL_TASK_UPSERT);
        bind(statement, "$1", entity.getId(), String.class);
        bind(statement, "$2", entity.getText(), String.class);
        bind(statement, "$3", entity.getDeadline(), LocalDateTime.class);
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
                .flatMap(result -> result.map((row, rowMetadata) -> row.get(0, String.class)))
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
