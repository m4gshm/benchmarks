package m4gshm.benchmark.rest.spring.boot.storage.querydsl.r2dbc;

import com.querydsl.core.QueryFlag;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLQuery;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import lombok.SneakyThrows;
import m4gshm.benchmark.rest.java.storage.ReactorStorage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.TypedNull;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.TaskSqlClauseHelper;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.QTask;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.QTaskTag;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.TaskDto;
import m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model.TaskTagDto;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.*;
import static m4gshm.benchmark.rest.spring.boot.storage.querydsl.TaskSqlClauseHelper.*;
import static reactor.core.publisher.Flux.fromIterable;
import static reactor.core.publisher.Mono.from;

public class TaskRepositoryImpl implements ReactorStorage<TaskImpl, String> {

    private final Configuration configuration;
    private final ConnectionFactory connectionFactory;

    public TaskRepositoryImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.configuration = new Configuration(new PostgreSQLTemplates() {{
            setNativeMerge(true);
        }});
    }

    @NotNull
    private static Publisher<TaskDto> mapResultToTaskPublisher(Result result) {
        return result.map((row, rowMetadata) -> {
            var dto = new TaskDto();
            dto.setId(row.get(QTask.task.id.getMetadata().getName(), String.class));
            dto.setText(row.get(QTask.task.text.getMetadata().getName(), String.class));
            dto.setDeadline(row.get(QTask.task.deadline.getMetadata().getName(), LocalDateTime.class));
            return dto;
        });
    }

    @NotNull
    private static Publisher<TaskTagDto> mapResultToTaskTagPublisher(Result result) {
        return result.map((row, rowMetadata) -> {
            var taskTagDto = new TaskTagDto();
            taskTagDto.setTaskId(row.get(0, String.class));
            taskTagDto.setTag(row.get(1, String.class));
            return taskTagDto;
        });
    }

    @NotNull
    private static <T> Flux<T> execSelect(Connection connection, SQLBindings sql, Function<Result, Publisher<T>> mapper) {
        return Flux.from(exec(connection, sql)).flatMap(mapper);
    }

    @NotNull
    private static Flux<Long> execUpdates(Connection connection, List<SQLBindings> sqls) {
        return fromIterable(sqls).flatMap(bindings -> exec(connection, bindings)).flatMap(Result::getRowsUpdated);
    }

    @NotNull
    private static Publisher<? extends Result> exec(Connection connection, SQLBindings bindings) {
        var sql = bindings.getSQL();
        var params = bindings.getNullFriendlyBindings();
        var statement = connection.createStatement(sql);
        for (int i = 0; i < params.size(); i++) {
            var param = params.get(i);
            if (param instanceof TypedNull typedNull) {
                statement.bindNull(i, typedNull.type());
            } else {
                statement.bind(i, param);
            }
        }
        return statement.execute();
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

    private Flux<TaskTagDto> selectTags(Connection connection, String taskId) {
        var query = selectTagsClause().where(QTaskTag.taskTag.taskId.eq(taskId));
        var sql = query.getSQL();
        return execSelect(connection, sql, TaskRepositoryImpl::mapResultToTaskTagPublisher);
    }

    private Flux<TaskTagDto> selectTagsIn(Connection connection, Collection<String> ids) {
        var query = selectTagsClause().where(QTaskTag.taskTag.taskId.in(ids));
        var sql = query.getSQL();
        return execSelect(connection, sql, TaskRepositoryImpl::mapResultToTaskTagPublisher);
    }

    @NotNull
    private SQLQuery<TaskTagDto> selectTagsClause() {
        return selectTagClause(null, configuration, true);
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

    @Override
    @SneakyThrows
    public Mono<TaskImpl> get(String id) {
        var sql = selectTaskByIdClause(null, configuration, true, id).getSQL();
        return connectMono(false, connection -> Mono.zip(
                execSelect(connection, sql, TaskRepositoryImpl::mapResultToTaskPublisher).next(),
                selectTags(connection, id).collectList(),
                TaskSqlClauseHelper::newTaskEntity)
        );
    }

    @Override
    @SneakyThrows
    public Flux<TaskImpl> getAll() {
        return connectFlux(false, connection -> execSelect(
                connection,
                selectTaskClause(null, configuration, true).getSQL(), TaskRepositoryImpl::mapResultToTaskPublisher).collectList()
                .flatMapMany(tasks -> {
                    var ids = tasks.stream().map(TaskDto::getId).collect(toSet());
                    return fromIterable(tasks).zipWith(selectTagsIn(connection, ids)
                                    .collect(groupingBy(TaskTagDto::getTaskId)),
                            (task, tags) -> newTaskEntity(task, tags.get(task.getId())));
                })
        );
    }

    @Override
    @SneakyThrows
    public Mono<TaskImpl> store(TaskImpl entity) {
        var id = entity.getId();
        return transactMono(connection -> {
            var upsert = execUpdates(connection, upsertTaskClause(null, configuration, true, entity, id).getSQL()).next();
            var tags = requireNonNullElse(entity.getTags(), List.<String>of());

            var tagsCondition = QTaskTag.taskTag.taskId.eq(id);
            tagsCondition = tags.isEmpty() ? tagsCondition : tagsCondition.and(QTaskTag.taskTag.tag.notIn(tags));

            var deleteUnusedTags = execUpdates(connection, newDeleteClause(null, configuration, QTaskTag.taskTag, true)
                    .where(tagsCondition).getSQL()).next();

            var insertTags = Flux.<Long>empty();
            if (!tags.isEmpty()) {
                var insertTagClause = newInsertClause(null, configuration, QTaskTag.taskTag, true);
                for (var tag : tags) {
                    insertTagClause
                            .set(QTaskTag.taskTag.tag, tag)
                            .set(QTaskTag.taskTag.taskId, id)
                            .addFlag(QueryFlag.Position.END, " on conflict do nothing")
                            .addBatch();
                }
                insertTags = execUpdates(connection, insertTagClause.getSQL());
            }

            return Flux.mergeSequential(
                            upsert,
                            deleteUnusedTags,
                            insertTags)
                    .collect(toList())
                    .map(results -> entity);
        });
    }

    @Override
    @SneakyThrows
    public Mono<Boolean> delete(String id) {
        return transactMono(connection -> {
            var deletedTags = execUpdates(connection, deleteTagByTaskIdClause(null, configuration, true, id).getSQL());
            var deletedTasks = execUpdates(connection, deleteTaskByIdClause(null, configuration, true, id).getSQL());

            return deletedTags.next().flatMap(deletedTagsCount -> deletedTasks.next()).map(deletedTaskCount -> deletedTaskCount > 0);
        });
    }

}
