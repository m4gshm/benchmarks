package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImplMeta.TaskColumn;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskTagImplMeta.TaskTagColumn;
import m4gshm.benchmark.rest.java.storage.model.impl.sql.TaskStorageQuery;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.POSTGRES_PLACEHOLDER;
import static reactor.core.publisher.Mono.from;

@RequiredArgsConstructor
public class TaskR2dbcRepositoryImpl implements TaskReactiveRepository<TaskImpl> {

    private static final TaskStorageQuery query = new TaskStorageQuery(POSTGRES_PLACEHOLDER, false);
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
                column.apply(builder, row.get(column.name(), column.type()));
            }
            return builder.build();
        });
    }

    @NotNull
    private static Publisher<Entry<String, String>> mapResultToTaskTagPublisher(Result result) {
        return result.map((row, rowMetadata) -> {
            var taskId = row.get(TaskTagColumn.task_id.name(), String.class);
            var tag = row.get(TaskTagColumn.tag.name(), String.class);
            return entry(taskId, tag);
        });
    }

    @NotNull
    private static Mono<Long> upsert(Connection connection, TaskImpl entity) {
        var statement = connection.createStatement(query.SQL_TASK_UPSERT);
        var columns = TaskColumn.values();
        var num = 0;
        for (var column : columns) {
            num++;
            var val = column.get(entity);
            bind(statement, "$" + num, val, column.type());
        }
        return Flux.from(statement.execute()).flatMap(Result::getRowsUpdated).next();
    }

    @NotNull
    private static Mono<Long> deleteUnusedTags(Connection connection, String taskId, Set<String> tags) {
        return tags == null || tags.isEmpty() ? Mono.empty() : Mono.defer(() -> {
            var statement = connection.createStatement(query.SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID);
            bind(statement, "$1", taskId, String.class);
            bind(statement, "$2", tags.toArray(new String[0]), String[].class);
            return Flux.from(statement.execute()).flatMap(Result::getRowsUpdated).next();
        });
    }

    @NotNull
    private static Mono<Long> insertTags(Connection connection, String taskId, Set<String> tags) {
        return tags == null || tags.isEmpty() ? Mono.empty() : Mono.defer(() -> {
            var statement = connection.createStatement(query.SQL_TASK_TAG_INSERT);
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
        return Flux.from(bind(connection.createStatement(query.SQL_TASK_TAG_SELECT_BY_TASK_ID), "$1", id, String.class)
                        .execute())
                .flatMap(result -> result.map((row, rowMetadata) -> {
                    return row.get(TaskTagColumn.tag.name(), String.class);
                }))
                .collect(toSet());
    }

    @NotNull
    private static Mono<Map<String, Set<String>>> getTasksTags(Connection connection, String[] ids) {
        return ids == null || ids.length == 0
                ? Mono.empty()
                : Flux.from(bind(connection.createStatement(query.SQL_TASK_TAG_SELECT_BY_TASK_IDS), "$1", ids, String[].class).execute())
                .flatMap(TaskR2dbcRepositoryImpl::mapResultToTaskTagPublisher)
                .collect(groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, toCollection(toLinkedHashSet()))));
    }

    @NotNull
    private static <T> Supplier<Set<T>> toLinkedHashSet() {
        return LinkedHashSet::new;
    }

    @NotNull
    private static Mono<TaskImpl> getTaskEntity(Connection connection, String id) {
        return Flux.from(bind(connection.createStatement(query.SQL_TASK_SELECT_BY_ID), "$1", id, String.class)
                        .fetchSize(1).execute())
                .flatMap(TaskR2dbcRepositoryImpl::mapResultToTaskPublisher).next();
    }

    @NotNull
    private static String[] getIds(List<TaskImpl> tasks) {
        return tasks != null ? tasks.stream().map(TaskImpl::getId).distinct().toArray(String[]::new) : null;
    }

    @Override
    public Flux<TaskImpl> findAll() {
        return connectFlux(false, connection -> Flux.from(connection.createStatement(query.SQL_TASK_SELECT_ALL).execute())
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
                        deleteUnusedTags(connection, entity.getId(), entity.getTags()),
                        insertTags(connection, entity.getId(), entity.getTags()))
                .collect(toList())
                .map(results -> entity));
    }

    @Override
    public Mono<? extends Number> deleteById(String id) {
        return connectMono(true, connection -> {
            var deleteTask = bind(connection.createStatement(query.SQL_TASK_DELETE_BY_ID), "$1", id, String.class).execute();
            var deleteTaskTags = bind(
                    connection.createStatement(query.SQL_TASK_TAG_DELETE_BY_TASK_ID), "$1", id, String.class
            ).execute();
            return Flux.mergeSequential(deleteTaskTags, deleteTask).flatMap(Result::getRowsUpdated)
                    .reduce(Long::sum).map(n -> (Number) n);
        });
    }

    @NotNull
    private Mono<? extends Connection> connection(boolean autoCommit) {
        var conn = connection();
        return autoCommit ? conn.flatMap(c -> from(c.setAutoCommit(true)).thenReturn(c)) : conn;
    }

    @NotNull
    private Mono<? extends Connection> connection() {
        return Mono.from(connectionFactory.create());
    }

    @NotNull
    private <T> Mono<T> connectMono(boolean autoCommit, Function<Connection, Mono<T>> routine) {
        return Mono.usingWhen(connection(autoCommit), routine, Connection::close);
    }

    @NotNull
    private <T> Mono<T> transactMono(Function<Connection, Mono<T>> routine) {
        return connectMono(false, c -> from(c.beginTransaction())
                .then(routine.apply(c))
                .flatMap(t -> from(c.commitTransaction()).thenReturn(t)));
    }

    @NotNull
    private <T> Flux<T> connectFlux(boolean autoCommit, Function<Connection, Flux<T>> routine) {
        return Flux.usingWhen(connection(autoCommit), routine, Connection::close);
    }

}
