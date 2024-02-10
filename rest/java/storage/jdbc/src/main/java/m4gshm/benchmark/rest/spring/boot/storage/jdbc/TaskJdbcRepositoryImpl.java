package m4gshm.benchmark.rest.spring.boot.storage.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImplMeta.TaskColumn;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskTagImplMeta;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskTagImplMeta.TaskTagColumn;
import m4gshm.benchmark.rest.java.storage.sql.SqlUtils;
import m4gshm.benchmark.rest.java.storage.sql.SqlUtils.ModifyDataSqlParts.ColumnPlaceholder;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl.TABLE_NAME_TASK;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl.TABLE_NAME_TASK_TAG;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.JDBC_PLACEHOLDER;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.ModifyDataSqlParts.newModifyDataSqlParts;

@RequiredArgsConstructor
public class TaskJdbcRepositoryImpl implements Storage<TaskImpl, String> {

    public static final String SQL_TASK_SELECT_ALL = SqlUtils.selectAll(TABLE_NAME_TASK, TaskColumn.values());
    public static final String SQL_TASK_UPSERT;
    public static final List<ColumnPlaceholder<TaskColumn<?, ?>>> SQL_TASK_UPSERT_PLACEHOLDERS;
    public static final String[] EMPTY_STRINGS = new String[0];
    public static final int[] EMPTY_INTS = new int[0];
    public static final String SQL_TASK_TAG_INSERT;
    public static final Map<TaskTagColumn<?>, Integer> SQL_TASK_TAG_INSERT_PLACEHOLDERS;
    private static final IntFunction<String> PLACEHOLDER = JDBC_PLACEHOLDER;
    public static final String SQL_TASK_SELECT_BY_ID = SqlUtils.selectBy(
            TABLE_NAME_TASK, TaskColumn.values(), TaskColumn.ID, PLACEHOLDER
    );
    public static final String SQL_TASK_DELETE_BY_ID = SqlUtils.deleteBy(TABLE_NAME_TASK, TaskColumn.ID, PLACEHOLDER);
    public static final String SQL_TASK_TAG_SELECT_BY_TASK_ID = SqlUtils.selectBy(
            TABLE_NAME_TASK_TAG, TaskTagColumn.values(), TaskTagColumn.TASK_ID, PLACEHOLDER
    );
    public static final String SQL_TASK_TAG_SELECT_BY_TASK_IDS = SqlUtils.selectByAny(
            TABLE_NAME_TASK_TAG, TaskTagColumn.values(), TaskTagColumn.TASK_ID, PLACEHOLDER
    );
    public static final String SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID = SqlUtils.deleteBy(
            TABLE_NAME_TASK_TAG, TaskTagColumn.TASK_ID, PLACEHOLDER
    ) + " AND NOT " + TaskTagColumn.TAG.name() + "=ANY(?)";
    public static final String SQL_TASK_TAG_DELETE_BY_TASK_ID = SqlUtils.deleteBy(TABLE_NAME_TASK_TAG, TaskTagColumn.TASK_ID, PLACEHOLDER);

    static {
        var taskTagDataParts = newModifyDataSqlParts(TaskTagColumn.values(), PLACEHOLDER);
        SQL_TASK_TAG_INSERT = SqlUtils.insert(TABLE_NAME_TASK_TAG, taskTagDataParts) + " ON CONFLICT DO NOTHING";

        var taskTagInsertPlaceholders = new HashMap<TaskTagColumn<?>, Integer>();
        var columnInsertPlaceholders = taskTagDataParts.columnInsertPlaceholders();
        for (var i = 0; i < columnInsertPlaceholders.size(); i++) {
            taskTagInsertPlaceholders.put(columnInsertPlaceholders.get(i).column(), i + 1);
        }
        SQL_TASK_TAG_INSERT_PLACEHOLDERS = unmodifiableMap(taskTagInsertPlaceholders);

        var taskDataParts = newModifyDataSqlParts(TaskColumn.values(), PLACEHOLDER);
        SQL_TASK_UPSERT = SqlUtils.upsert(TABLE_NAME_TASK, taskDataParts);

        var insertPlaceholders = taskDataParts.columnInsertPlaceholders();
        var upsertPlaceholders = taskDataParts.columnUpsertPlaceholders();

        SQL_TASK_UPSERT_PLACEHOLDERS = Stream.concat(insertPlaceholders.stream(), upsertPlaceholders.stream()).toList();
    }

    private final DataSource dataSource;

    @NotNull
    @SneakyThrows
    private static List<TaskImpl> toTaskImpList(ResultSet resultSet) {
        var tasks = new ArrayList<TaskImpl>();
        while (resultSet.next()) {
            tasks.add(newTaskImp(resultSet));
        }
        return tasks;
    }

    @SneakyThrows
    private static TaskImpl newTaskImp(ResultSet resultSet) {
        var builder = TaskImpl.builder();
        for (TaskColumn column : TaskColumn.values()) {
            populate(resultSet, column, builder);
        }
        return builder.build();
    }

    private static <T, B> void populate(ResultSet resultSet, TaskColumn<T, B> column, B builder) throws SQLException {
        var type = column.type();
        if (LocalDateTime.class.equals(type)) {
            var localDateTime = toLocalDateTime(resultSet.getObject(column.name(), Timestamp.class));
            ((TaskColumn<LocalDateTime, B>) column).apply(builder, localDateTime);
        } else {
            column.apply(builder, resultSet.getObject(column.name(), type));
        }
    }

    private static LocalDateTime toLocalDateTime(Timestamp date) {
        return date != null ? LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault()) : null;
    }

    @NotNull
    @SneakyThrows
    private static Map<String, LinkedHashSet<String>> toTaskTagsMap(ResultSet resultSet) {
        var tags = new HashMap<String, LinkedHashSet<String>>();
        while (resultSet.next()) {
            var taskId = resultSet.getObject(TaskTagColumn.TASK_ID.name(), TaskTagColumn.TASK_ID.type());
            var tag = resultSet.getObject(TaskTagColumn.TAG.name(), TaskTagColumn.TAG.type());
            tags.computeIfAbsent(taskId, s -> new LinkedHashSet<>()).add(tag);
        }
        return tags;
    }

    @NotNull
    @SneakyThrows
    private static LinkedHashSet<String> toTaskTagsSet(ResultSet resultSet) {
        var tags = new LinkedHashSet<String>();
        while (resultSet.next()) {
            var tag = resultSet.getObject(TaskTagColumn.TAG.name(), TaskTagColumn.TAG.type());
            tags.add(tag);
        }
        return tags;
    }

    @SneakyThrows
    private static int upsert(Connection connection, TaskImpl entity) {
        try (var statement = connection.prepareStatement(SQL_TASK_UPSERT)) {
            var upsertPlaceholders = SQL_TASK_UPSERT_PLACEHOLDERS;
            for (var i = 0; i < upsertPlaceholders.size(); i++) {
                var placeholder = upsertPlaceholders.get(i);
                var column = placeholder.column();
                var num = i + 1;
                var value = column.get(entity);
                statement.setObject(num, value instanceof LocalDateTime ldt ? toTimestamp(ldt) : value);
            }
            return statement.executeUpdate();
        }
    }

    private static java.sql.Timestamp toTimestamp(LocalDateTime localDateTime) {
        return localDateTime == null ? null : new java.sql.Timestamp(
                localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
    }

    @SneakyThrows
    private static int deleteUnusedTags(Connection connection, String taskId, Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return 0;
        }
        try (var statement = connection.prepareStatement(SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID)) {
            statement.setString(1, taskId);
            statement.setArray(2, newPgArray(connection, tags.toArray(new String[0])));
            return statement.executeUpdate();
        }
    }

    @NotNull
    @SneakyThrows
    private static int[] insertTags(Connection connection, String taskId, Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return EMPTY_INTS;
        }
        try (var statement = connection.prepareStatement(SQL_TASK_TAG_INSERT)) {
            for (var tag : tags) {
                statement.setString(SQL_TASK_TAG_INSERT_PLACEHOLDERS.get(TaskTagColumn.TASK_ID), taskId);
                statement.setString(SQL_TASK_TAG_INSERT_PLACEHOLDERS.get(TaskTagColumn.TAG), tag);
                statement.addBatch();
            }
            return statement.executeBatch();
        }
    }

    @NotNull
    @SneakyThrows
    private static LinkedHashSet<String> getTaskTags(Connection connection, String id) {
        try (var statement = connection.prepareStatement(SQL_TASK_TAG_SELECT_BY_TASK_ID)) {
            statement.setString(1, id);
            try (var resultSet = statement.executeQuery()) {
                return toTaskTagsSet(resultSet);
            }
        }
    }

    @NotNull
    @SneakyThrows
    private static Map<String, LinkedHashSet<String>> getTasksTags(Connection connection, String[] ids) {
        if (ids == null || ids.length == 0) {
            return Map.of();
        }
        try (var statement = connection.prepareStatement(SQL_TASK_TAG_SELECT_BY_TASK_IDS)) {
            statement.setArray(1, newPgArray(connection, ids));
            try (var resultSet = statement.executeQuery()) {
                return toTaskTagsMap(resultSet);
            }
        }
    }

    @SneakyThrows
    private static Array newPgArray(Connection connection, String[] ids) {
        return connection.createArrayOf("text", ids);
    }

    @SneakyThrows
    private static TaskImpl getTask(Connection connection, String id) {
        try (var statement = connection.prepareStatement(SQL_TASK_SELECT_BY_ID)) {
            statement.setString(1, id);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next() ? newTaskImp(resultSet) : null;
            }
        }
    }

    @NotNull
    private static String[] getIds(List<TaskImpl> tasks) {
        return tasks != null ? tasks.stream().map(TaskImpl::getId).distinct().toArray(String[]::new) : EMPTY_STRINGS;
    }

    @NotNull
    private static List<TaskImpl> getTasks(Connection connection) throws SQLException {
        List<TaskImpl> tasks;
        try (var stmt = connection.prepareStatement(SQL_TASK_SELECT_ALL);
             var rs = stmt.executeQuery()) {
            tasks = toTaskImpList(rs);
        }
        return tasks;
    }

    private static TaskImpl withTags(TaskImpl task, Set<String> tags) {
        return tags != null && !tags.isEmpty() ? task.toBuilder().tags(tags).build() : task;
    }

    @Override
    @SneakyThrows
    public List<TaskImpl> getAll() {
        try (var connection = dataSource.getConnection()) {
            var tasks = getTasks(connection);
            var tagsByTaskId = getTasksTags(connection, getIds(tasks));
            return tasks.stream().map(task -> withTags(task, tagsByTaskId.get(task.getId()))).toList();
        }
    }

    @Override
    @SneakyThrows
    public TaskImpl get(String id) {
        try (var connection = dataSource.getConnection()) {
            return withTags(getTask(connection, id), getTaskTags(connection, id));
        }
    }

    @Override
    @SneakyThrows
    public TaskImpl store(TaskImpl entity) {
        try (var connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                upsert(connection, entity);
                deleteUnusedTags(connection, entity.getId(), entity.getTags());
                insertTags(connection, entity.getId(), entity.getTags());
                connection.commit();
                return entity;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    @SneakyThrows
    public boolean delete(String id) {
        try (var connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                try (var statement = connection.prepareStatement(SQL_TASK_TAG_DELETE_BY_TASK_ID)) {
                    statement.setString(1, id);
                    statement.execute();
                }
                int deletedCount;
                try (var statement = connection.prepareStatement(SQL_TASK_DELETE_BY_ID)) {
                    statement.setString(1, id);
                    deletedCount = statement.executeUpdate();
                }
                connection.commit();
                return deletedCount > 0;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }
}
