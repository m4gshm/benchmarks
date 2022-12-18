package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.spring.boot.storage.r2dbc.model.TaskEntity;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.function.Function;

import static reactor.core.publisher.Mono.from;

@RequiredArgsConstructor
public class TaskEntityRepositoryImpl implements TaskEntityRepository<TaskEntity> {

    private final ConnectionFactory connectionFactory;

    @NotNull
    private static <T> Statement bind(Statement statement, String name, T value, Class<T> clazz) {
        return value != null ? statement.bind(name, value) : statement.bindNull(name, clazz);
    }

    @Override
    public Mono<TaskEntity> findById(String id) {
        return conn(false).flatMapMany(connection -> bind(connection.createStatement("SELECT " +
                        TaskEntity.TASK_COLUMN_TEXT + "," +
                        TaskEntity.TASK_COLUMN_DEADLINE +
                        " FROM " + TaskEntity.TABLE_NAME_TASK +
                        " WHERE " + TaskEntity.TASK_COLUMN_ID + " = $1"
                ), "$1", id, String.class).fetchSize(1).execute()
        ).flatMap(result -> result.map((row, rowMetadata) -> TaskEntity.builder()
                .id(id)
                .text(row.get(TaskEntity.TASK_COLUMN_TEXT, String.class))
                .deadline(row.get(TaskEntity.TASK_COLUMN_DEADLINE, LocalDateTime.class))
                .build())
        ).next();
    }

    @Override
    public Mono<TaskEntity> save(TaskEntity entity) {
        return inTrans(connection -> {
            var deadline = entity.getDeadline();
            var text = entity.getText();
            var statement = connection.createStatement("INSERT INTO " +
                    TaskEntity.TABLE_NAME_TASK +
                    "(" +
                    TaskEntity.TASK_COLUMN_ID + "," +
                    TaskEntity.TASK_COLUMN_TEXT + "," +
                    TaskEntity.TASK_COLUMN_DEADLINE +
                    ") " +
                    "VALUES ($1,$2,$3) " +
                    "ON CONFLICT (" + TaskEntity.TASK_COLUMN_ID +
                    ") DO UPDATE SET " +
                    TaskEntity.TASK_COLUMN_TEXT + " = $2," +
                    TaskEntity.TASK_COLUMN_DEADLINE + " = $3"
            );
            bind(statement, "$1", entity.getId(), String.class);
            bind(statement, "$2", text, String.class);
            bind(statement, "$3", deadline, LocalDateTime.class);
            return Flux.from(statement.execute()).flatMap(Result::getRowsUpdated).next()
                    .map(result -> entity);
        });
    }

    @Override
    public Mono<? extends Number> deleteById(String id) {
        return conn(true).flatMapMany(connection -> bind(connection.createStatement(
                        "DELETE FROM " + TaskEntity.TABLE_NAME_TASK + " WHERE " + TaskEntity.TASK_COLUMN_ID + "=$1"
                ), "$1", id, String.class).execute()).flatMap(Result::getRowsUpdated).next();
    }

    @Override
    public Flux<TaskEntity> findAll() {
        return conn(false).flatMapMany(connection -> connection.createStatement("SELECT " +
                        TaskEntity.TASK_COLUMN_ID + "," +
                        TaskEntity.TASK_COLUMN_TEXT + "," +
                        TaskEntity.TASK_COLUMN_DEADLINE +
                        " FROM " + TaskEntity.TABLE_NAME_TASK
                ).execute()
        ).flatMap(result -> result.map((row, rowMetadata) -> TaskEntity.builder()
                .id(row.get(TaskEntity.TASK_COLUMN_ID, String.class))
                .text(row.get(TaskEntity.TASK_COLUMN_TEXT, String.class))
                .deadline(row.get(TaskEntity.TASK_COLUMN_DEADLINE, LocalDateTime.class))
                .build())
        );
    }

    @NotNull
    private Mono<? extends Connection> conn(boolean autoCommit) {
        var conn = conn();
        return autoCommit ? conn.flatMap(c -> from(c.setAutoCommit(true)).thenReturn(c)) : conn;
    }

    @NotNull
    private Mono<? extends Connection> conn() {
        return from(connectionFactory.create());
    }

    @NotNull
    private <T> Mono<T> conn(boolean autoCommit, Function<Connection, Mono<T>> routine) {
        return conn(autoCommit).flatMap(routine);
    }

    @NotNull
    private <T> Mono<T> inTrans(Function<Connection, Mono<T>> routine) {
        return conn().flatMap(c ->
                from(c.beginTransaction())
                        .thenReturn(c).flatMap(routine)
                        .flatMap(t -> from(c.commitTransaction()).thenReturn(t))
        );
    }
}
