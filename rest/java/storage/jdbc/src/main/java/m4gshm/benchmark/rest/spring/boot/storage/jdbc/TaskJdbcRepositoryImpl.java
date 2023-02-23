package m4gshm.benchmark.rest.spring.boot.storage.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static m4gshm.benchmark.rest.java.storage.model.impl.TaskTableHelper.*;

@RequiredArgsConstructor
public class TaskJdbcRepositoryImpl implements Storage<TaskImpl, String> {

    public static final String SQL_TASK_SELECT_ALL = "SELECT " +
            TASK_COLUMN_ID + "," +
            TASK_COLUMN_TEXT + "," +
            TASK_COLUMN_DEADLINE +
            " FROM " + TABLE_NAME_TASK;
    public static final String SQL_TASK_SELECT_BY_ID = SQL_TASK_SELECT_ALL + " WHERE " + TASK_COLUMN_ID + " = ?";
    public static final String SQL_TASK_UPSERT = "INSERT INTO " +
            TABLE_NAME_TASK +
            "(" +
            TASK_COLUMN_ID + "," +
            TASK_COLUMN_TEXT + "," +
            TASK_COLUMN_DEADLINE +
            ") VALUES (?,?,?) " +
            "ON CONFLICT (" + TASK_COLUMN_ID +
            ") DO UPDATE SET " +
            TASK_COLUMN_TEXT + " = ?," +
            TASK_COLUMN_DEADLINE + " = ?";
    public static final String SQL_TASK_DELETE_BY_ID = "DELETE FROM " + TABLE_NAME_TASK + " WHERE " + TASK_COLUMN_ID + "=?";

    public static final String SQL_TASK_TAG_SELECT_BY_TASK_ID = "SELECT " + TASK_TAG_COLUMN_TAG + " FROM " +
            TABLE_NAME_TASK_TAG + " WHERE " + TASK_TAG_COLUMN_TASK_ID + "=?";

    public static final String SQL_TASK_TAG_SELECT_BY_TASK_IDS = "SELECT " + TASK_TAG_COLUMN_TASK_ID + "," +
            TASK_TAG_COLUMN_TAG + " FROM " + TABLE_NAME_TASK_TAG + " WHERE " +
            TASK_TAG_COLUMN_TASK_ID + " = any(?)";

    public static final String SQL_TASK_TAG_INSERT = "INSERT INTO " + TABLE_NAME_TASK_TAG +
            "(" +
            TASK_TAG_COLUMN_TASK_ID + "," +
            TASK_TAG_COLUMN_TAG +
            ") VALUES (?,?) ON CONFLICT DO NOTHING";

    public static final String SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID = "DELETE FROM " + TABLE_NAME_TASK_TAG +
            " WHERE " + TASK_TAG_COLUMN_TASK_ID + "=? AND NOT " + TASK_TAG_COLUMN_TAG + "=ANY(?)";

    public static final String SQL_TASK_TAG_DELETE_BY_TASK_ID = "DELETE FROM " + TABLE_NAME_TASK_TAG +
            " WHERE " + TASK_TAG_COLUMN_TASK_ID + "=?";
    public static final String[] EMPTY_STRINGS = new String[0];
    public static final int[] EMPTY_INTS = new int[0];

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
        return TaskImpl.builder()
                .id(resultSet.getString(TASK_COLUMN_ID))
                .text(resultSet.getString(TASK_COLUMN_TEXT))
                .deadline(toLocalDateTime(resultSet.getTimestamp(TASK_COLUMN_DEADLINE)))
                .build();
    }

    private static LocalDateTime toLocalDateTime(Timestamp date) {
        return date != null ? LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault()) : null;
    }

    @NotNull
    @SneakyThrows
    private static Map<String, LinkedHashSet<String>> toTaskTagsMap(ResultSet resultSet) {
        var tags = new HashMap<String, LinkedHashSet<String>>();
        while (resultSet.next()) {
            var taskId = resultSet.getString(1);
            var tag = resultSet.getString(2);
            tags.computeIfAbsent(taskId, s -> new LinkedHashSet<>()).add(tag);
        }
        return tags;
    }

    @NotNull
    @SneakyThrows
    private static LinkedHashSet<String> toTaskTagsSet(ResultSet resultSet) {
        var tags = new LinkedHashSet<String>();
        while (resultSet.next()) {
            var tag = resultSet.getString(1);
            tags.add(tag);
        }
        return tags;
    }

    @NotNull
    @SneakyThrows
    private static int upsert(Connection connection, TaskImpl entity) {
        try (var statement = connection.prepareStatement(SQL_TASK_UPSERT)) {
            var deadline = toTimestamp(entity.getDeadline());
            var id = entity.getId();
            var text = entity.getText();
            statement.setString(1, id);
            statement.setString(2, text);
            statement.setTimestamp(3, deadline);
            statement.setString(4, text);
            statement.setTimestamp(5, deadline);
            return statement.executeUpdate();
        }
    }

    private static java.sql.Timestamp toTimestamp(LocalDateTime localDateTime) {
        return localDateTime == null ? null : new java.sql.Timestamp(
                localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
    }

    @NotNull
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
            for (var iterator = tags.iterator(); iterator.hasNext(); ) {
                var tag = iterator.next();
                statement.setString(1, taskId);
                statement.setString(2, tag);
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
        return tasks != null ? tasks.stream().map(TaskImpl::id).distinct().toArray(String[]::new) : EMPTY_STRINGS;
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
        return tags != null && !tags.isEmpty()
                ? task.toBuilder().tags(tags).build() : task;
    }

    @Override
    @SneakyThrows
    public List<TaskImpl> getAll() {
        try (var connection = dataSource.getConnection()) {
            var tasks = getTasks(connection);
            var tagsByTaskId = getTasksTags(connection, getIds(tasks));
            return tasks.stream().map(task -> withTags(task, tagsByTaskId.get(task.id()))).toList();
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
                deleteUnusedTags(connection, entity.id(), entity.tags());
                insertTags(connection, entity.id(), entity.tags());
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
