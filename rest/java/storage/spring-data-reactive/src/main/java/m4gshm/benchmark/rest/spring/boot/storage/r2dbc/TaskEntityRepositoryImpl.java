package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.spring.boot.storage.r2dbc.model.TaskEntity;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static reactor.core.publisher.Mono.from;

@RequiredArgsConstructor
public class TaskEntityRepositoryImpl implements TaskEntityRepository<TaskEntity> {

    public static final String SQL_TASK_SELECT_ALL = "SELECT " +
            TaskEntity.TASK_COLUMN_ID + "," +
            TaskEntity.TASK_COLUMN_TEXT + "," +
            TaskEntity.TASK_COLUMN_DEADLINE +
            " FROM " + TaskEntity.TABLE_NAME_TASK;
    public static final String SQL_TASK_SELECT_BY_ID = SQL_TASK_SELECT_ALL + " WHERE " + TaskEntity.TASK_COLUMN_ID + " = $1";
    public static final String SQL_TASK_UPSERT = "INSERT INTO " +
            TaskEntity.TABLE_NAME_TASK +
            "(" +
            TaskEntity.TASK_COLUMN_ID + "," +
            TaskEntity.TASK_COLUMN_TEXT + "," +
            TaskEntity.TASK_COLUMN_DEADLINE +
            ") VALUES ($1,$2,$3) " +
            "ON CONFLICT (" + TaskEntity.TASK_COLUMN_ID +
            ") DO UPDATE SET " +
            TaskEntity.TASK_COLUMN_TEXT + " = $2," +
            TaskEntity.TASK_COLUMN_DEADLINE + " = $3";
    public static final String SQL_TASK_DELETE_BY_ID = "DELETE FROM " + TaskEntity.TABLE_NAME_TASK + " WHERE " + TaskEntity.TASK_COLUMN_ID + "=$1";

    public static final String SQL_TASK_TAG_SELECT_BY_TASK_ID = "SELECT " + TaskEntity.TASK_TAG_COLUMN_TAG + " FROM " +
            TaskEntity.TABLE_NAME_TASK_TAG + " WHERE " + TaskEntity.TASK_TAG_COLUMN_TASK_ID + "=$1";

    public static final String SQL_TASK_TAG_INSERT = "INSERT INTO " + TaskEntity.TABLE_NAME_TASK_TAG +
            "(" +
            TaskEntity.TASK_TAG_COLUMN_TASK_ID + "," +
            TaskEntity.TASK_TAG_COLUMN_TAG +
            ") VALUES ($1,$2) ON CONFLICT DO NOTHING";

    public static final String SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID = "DELETE FROM " + TaskEntity.TABLE_NAME_TASK_TAG +
            " WHERE " + TaskEntity.TASK_TAG_COLUMN_TASK_ID + "=$1 AND NOT " + TaskEntity.TASK_TAG_COLUMN_TAG + "=ANY($2)";

    public static final String SQL_TASK_TAG_DELETE_BY_TASK_ID = "DELETE FROM " + TaskEntity.TABLE_NAME_TASK_TAG +
            " WHERE " + TaskEntity.TASK_TAG_COLUMN_TASK_ID + "=$1";

    private final ConnectionFactory connectionFactory;

    @NotNull
    private static <T> Statement bind(Statement statement, String name, T value, Class<T> clazz) {
        return value != null ? statement.bind(name, value) : statement.bindNull(name, clazz);
    }

    @NotNull
    private static Publisher<TaskEntity> mapResultToTaskPublisher(Result result) {
        return result.map((row, rowMetadata) -> TaskEntity.builder()
                .id(row.get(TaskEntity.TASK_COLUMN_ID, String.class))
                .text(row.get(TaskEntity.TASK_COLUMN_TEXT, String.class))
                .deadline(row.get(TaskEntity.TASK_COLUMN_DEADLINE, LocalDateTime.class))
                .build());
    }

    @NotNull
    private static Mono<Integer> upsert(Connection connection, TaskEntity entity) {
        var statement = connection.createStatement(SQL_TASK_UPSERT);
        bind(statement, "$1", entity.getId(), String.class);
        bind(statement, "$2", entity.getText(), String.class);
        bind(statement, "$3", entity.getDeadline(), LocalDateTime.class);
        return Flux.from(statement.execute()).flatMap(Result::getRowsUpdated).next();
    }

    @NotNull
    private static Mono<Integer> deleteUnusedTags(Connection connection, String taskId, Set<String> tags) {
        var statement = connection.createStatement(SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID);
        bind(statement, "$1", taskId, String.class);
        bind(statement, "$2", tags.toArray(new String[0]), String[].class);
        return Flux.from(statement.execute()).flatMap(Result::getRowsUpdated).next();
    }

    @NotNull
    private static Mono<Integer> insertTags(Connection connection, String taskId, Set<String> tags) {
        var statement = connection.createStatement(SQL_TASK_TAG_INSERT);
        bind(statement, "$1", taskId, String.class);
        bind(statement, "$2", tags.toArray(new String[0]), String[].class);
        return Flux.from(statement.execute()).flatMap(Result::getRowsUpdated).next();
    }

    @NotNull
    private static Mono<Set<String>> getTaskTags(String id, Connection connection) {
        return Flux.from(bind(connection.createStatement(SQL_TASK_TAG_SELECT_BY_TASK_ID), "$1", id, String.class).execute()).flatMap(result -> result.map((row, rowMetadata) -> row.get(0, String.class))).collect(Collectors.toSet());
    }

    @NotNull
    private static Mono<TaskEntity> getTaskEntity(String id, Connection connection) {
        return Flux.from(bind(connection.createStatement(SQL_TASK_SELECT_BY_ID), "$1", id, String.class).fetchSize(1).execute()).flatMap(TaskEntityRepositoryImpl::mapResultToTaskPublisher).next();
    }

    @Override
    public Mono<TaskEntity> findById(String id) {
        return connMono(false, connection -> Mono.zip(
                getTaskEntity(id, connection), getTaskTags(id, connection),
                (taskEntity, strings) -> taskEntity.toBuilder().tags(strings).build()));
    }

    @Override
    public Mono<TaskEntity> save(TaskEntity entity) {
        return transactMono(connection -> Flux.mergeSequential(
                        upsert(connection, entity),
                        deleteUnusedTags(connection, entity.id(), entity.tags()),
                        insertTags(connection, entity.id(), entity.tags()))
                .collect(toList())
                .map(results -> entity));
    }

    @Override
    public Mono<? extends Number> deleteById(String id) {
        return connMono(true, connection -> {
            var deleteTask = bind(connection.createStatement(SQL_TASK_DELETE_BY_ID), "$1", id, String.class).execute();
            var deleteTaskTags = bind(
                    connection.createStatement(SQL_TASK_TAG_DELETE_BY_TASK_ID), "$1", id, String.class
            ).execute();
            return Flux.mergeSequential(deleteTask, deleteTaskTags).flatMap(Result::getRowsUpdated)
                    .reduce(Integer::sum).map(n -> (Number) n);
        });
    }

    @Override
    public Flux<TaskEntity> findAll() {
        return connFlux(false, connection -> Flux.from(connection.createStatement(SQL_TASK_SELECT_ALL)
                .execute()).flatMap(result -> mapResultToTaskPublisher(result)));
    }

    @NotNull
    private Mono<? extends Connection> conn(boolean autoCommit) {
        var conn = conn();
        return autoCommit ? conn.flatMap(c -> from(c.setAutoCommit(true)).thenReturn(c)) : conn;
    }

    @NotNull
    private Mono<? extends Connection> conn() {
        return Mono.from(connectionFactory.create());
    }

    @NotNull
    private <T> Mono<T> connMono(boolean autoCommit, Function<Connection, Mono<T>> routine) {
        return Mono.usingWhen(conn(autoCommit), routine, Connection::close);
    }

    @NotNull
    private <T> Mono<T> transactMono(Function<Connection, Mono<T>> routine) {
        return connMono(false, c -> from(c.beginTransaction())
                .then(routine.apply(c))
                .flatMap(t -> from(c.commitTransaction()).thenReturn(t)));
    }

    @NotNull
    private <T> Flux<T> connFlux(boolean autoCommit, Function<Connection, Flux<T>> routine) {
        return Flux.usingWhen(conn(autoCommit), routine, Connection::close);
    }
}
