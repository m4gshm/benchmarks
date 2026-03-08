package m4gshm.benchmark.rest.spring.boot.storage.jooq.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import java.util.*;

import static io.github.m4gshm.benchmark.rest.data.access.jooq.Tables.TASK;
import static io.github.m4gshm.benchmark.rest.data.access.jooq.Tables.TASK_TAG;
import static java.util.stream.Collectors.*;
import static m4gshm.benchmark.rest.java.storage.model.impl.sql.TaskStorageConstants.EMPTY_INTS;
import static m4gshm.benchmark.rest.java.storage.model.impl.sql.TaskStorageConstants.EMPTY_STRINGS;
import static org.jooq.impl.DSL.excluded;

@RequiredArgsConstructor
public class TaskStorageJooqImpl implements Storage<TaskImpl, String> {

    private final DSLContext dsl;

    @SneakyThrows
    private static TaskImpl newTaskImp(Record record) {
        return TaskImpl.builder()
                .id(record.get(TASK.ID))
                .deadline(record.get(TASK.DEADLINE))
                .text(record.get(TASK.TEXT))
                .build();
    }

    @SneakyThrows
    private static int upsert(DSLContext dsl, TaskImpl entity) {
        return dsl.insertInto(TASK)
                .set(TASK.ID, entity.getId())
                .set(TASK.TEXT, entity.getText())
                .set(TASK.DEADLINE, entity.deadline())
                .onDuplicateKeyUpdate()
                .set(TASK.TEXT, excluded(TASK.TEXT))
                .set(TASK.DEADLINE, excluded(TASK.DEADLINE))
                .execute();
    }

    @SneakyThrows
    private static int deleteUnusedTags(DSLContext dsl, String taskId, Set<String> tags) {
        return tags == null || tags.isEmpty() ? 0
                : dsl.deleteFrom(TASK_TAG).where(TASK_TAG.TASK_ID.eq(taskId)
                .and(TASK_TAG.TAG.notIn(tags))).execute();
    }

    @NotNull
    @SneakyThrows
    private static int[] insertTags(DSLContext dsl, String taskId, Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return EMPTY_INTS;
        }

        var inserts = tags.stream().map(tag -> {
            return dsl.insertInto(TASK_TAG)
                    .set(TASK_TAG.TASK_ID, taskId)
                    .set(TASK_TAG.TAG, tag)
                    .onConflictDoNothing();
        }).toList();

        return inserts.stream().mapToInt(Query::execute).toArray();
    }

    @NotNull
    @SneakyThrows
    private static Map<String, LinkedHashSet<String>> getTasksTags(DSLContext dsl, String[] ids) {
        return ids == null || ids.length == 0 ? Map.of()
                : dsl.select(TASK_TAG.fields()).from(TASK_TAG).where(TASK_TAG.TASK_ID.in(ids)).fetch().stream()
                .collect(groupingBy(r -> {
                    return r.get(TASK_TAG.TASK_ID);
                }, mapping(r -> {
                    return r.get(TASK_TAG.TAG);
                }, toCollection(LinkedHashSet::new))));
    }

    @NotNull
    private static String[] getIds(List<TaskImpl> tasks) {
        return tasks != null ? tasks.stream().map(TaskImpl::getId).distinct().toArray(String[]::new) : EMPTY_STRINGS;
    }

    private static TaskImpl withTags(TaskImpl task, Set<String> tags) {
        return tags != null && !tags.isEmpty() ? task.toBuilder().tags(tags).build() : task;
    }

    @NotNull
    private static SelectJoinStep<Record> getTasks(DSLContext dsl) {
        return dsl.select(TASK.fields()).from(TASK);
    }

    @Override
    @SneakyThrows
    public List<TaskImpl> getAll() {
        var tasks = getTasks(dsl).fetch().stream().map(TaskStorageJooqImpl::newTaskImp).toList();

        var tagsByTaskId = getTasksTags(dsl, getIds(tasks));
        return tasks.stream().map(task -> {
            return withTags(task, tagsByTaskId.get(task.getId()));
        }).toList();
    }

    @Override
    @SneakyThrows
    public TaskImpl get(String id) {
        return getTasks(dsl).where(TASK.ID.eq(id))
                .fetchOptional().map(TaskStorageJooqImpl::newTaskImp)
                .orElse(null);
    }

    @Override
    @SneakyThrows
    public TaskImpl store(TaskImpl entity, String id) {
        return dsl.transactionResult(configuration -> {
            upsert(dsl, entity);
            deleteUnusedTags(dsl, id, entity.getTags());
            insertTags(dsl, id, entity.getTags());
            return entity;
        });
    }

    @Override
    @SneakyThrows
    public boolean delete(String id) {
        var batch = dsl.batch(
                dsl.delete(TASK_TAG).where(TASK_TAG.TASK_ID.eq(id)),
                dsl.delete(TASK).where(TASK.ID.eq(id))
        );
        var deletedCounts = batch.execute();
        return Arrays.stream(deletedCounts).anyMatch(i -> {
            return i > 0;
        });
    }
}
