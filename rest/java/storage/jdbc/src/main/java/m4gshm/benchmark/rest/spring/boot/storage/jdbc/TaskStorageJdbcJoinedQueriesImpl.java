package m4gshm.benchmark.rest.spring.boot.storage.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImplMeta.TaskColumn;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskTagImplMeta.TaskTagColumn;
import m4gshm.benchmark.rest.java.storage.model.impl.sql.TaskStorageQuery;
import meta.util.Typed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static m4gshm.benchmark.rest.java.storage.model.impl.sql.TaskStorageConstants.EMPTY_STRINGS;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.JDBC_PLACEHOLDER;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.POSTGRES_PLACEHOLDER;

@RequiredArgsConstructor
public class TaskStorageJdbcJoinedQueriesImpl implements Storage<TaskImpl, String> {

    private static final TaskStorageQuery query = new TaskStorageQuery(JDBC_PLACEHOLDER, true);
    private static final TaskStorageQuery queryTemplates = new TaskStorageQuery(POSTGRES_PLACEHOLDER, true);
    private final DataSource dataSource;

    @NotNull
    @SneakyThrows
    private static List<TaskImpl> toTaskImpList(ResultSet resultSet, Map<? extends TaskColumn<?>, Alias> taskColAliases, Alias tagColumnAlias) {
        var tasks = new ArrayList<TaskImpl>();
        var tags = new LinkedHashSet<String>();
        TaskImpl prevTask = null;
        while (resultSet.next()) {
            var task = newTaskImp(resultSet, taskColAliases);
            if (prevTask == null) {
                prevTask = task;
            } else if (!prevTask.id().equals(task.id())) {
                tasks.add(prevTask.withTags(tags));
                tags = new LinkedHashSet<>();
                prevTask = task;
            }
            tags.add(getTagValue(resultSet, tagColumnAlias.name));
        }
        if (prevTask != null) {
            tasks.add(prevTask.withTags(tags));
        }
        return tasks;
    }

    @SneakyThrows
    private static TaskImpl newTaskImp(ResultSet resultSet, Map<? extends TaskColumn<?>, Alias> aliases) {
        var builder = TaskImpl.builder();
        for (var column : TaskColumn.values()) {
            populate(resultSet, column, builder, taskColumn -> {
                var alias = aliases.get(column);
                return ofNullable(alias).map(a -> a.name).orElse(taskColumn.name());
            });
        }
        return builder.build();
    }

    private static <T> void populate(ResultSet resultSet, TaskColumn<T> column, TaskImpl.TaskImplBuilder builder,
                                     Function<TaskColumn<?>, String> nameExtractor) throws SQLException {
        var type = column.type();
        if (LocalDateTime.class.equals(type)) {
            var localDateTime = toLocalDateTime(resultSet.getObject(nameExtractor.apply(column), Timestamp.class));
            ((TaskColumn<LocalDateTime>) column).apply(builder, localDateTime);
        } else {
            column.apply(builder, resultSet.getObject(nameExtractor.apply(column), type));
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
            var taskId = resultSet.getObject(TaskTagColumn.task_id.name(), TaskTagColumn.task_id.type());
            String tagValue = getTagValue(resultSet, TaskTagColumn.tag.name());
            tags.computeIfAbsent(taskId, s -> new LinkedHashSet<>()).add(tagValue);
        }
        return tags;
    }


    private static String getTagValue(ResultSet resultSet, String columnLabel) throws SQLException {
        return resultSet.getObject(columnLabel, TaskTagColumn.tag.type());
    }

    @NotNull
    @SneakyThrows
    private static Map<String, LinkedHashSet<String>> getTasksTags(Connection connection, String[] ids) {
        if (ids == null || ids.length == 0) {
            return Map.of();
        }
        try (var statement = connection.prepareStatement(query.SQL_TASK_TAG_SELECT_BY_TASK_IDS)) {
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

    @NotNull
    private static String[] getIds(List<TaskImpl> tasks) {
        return tasks != null ? tasks.stream().map(TaskImpl::getId).distinct().toArray(String[]::new) : EMPTY_STRINGS;
    }

    @NotNull
    private static List<TaskImpl> getTasks(Connection connection) throws SQLException {
        List<TaskImpl> tasks;
        var sqlParts = getSelectAll("t", "tt", "");
        try (var stmt = connection.prepareStatement(sqlParts.sqlJoin);
             var rs = stmt.executeQuery()) {
            tasks = toTaskImpList(rs, sqlParts.taskColumnAliases, sqlParts.taskTagsColumnAlias);
        }
        return tasks;
    }

    private static TaskImpl withTags(TaskImpl task, Set<String> tags) {
        return tags != null && !tags.isEmpty() ? task.withTags(tags) : task;
    }

    private static String toSqlVal(Object value) {
        return toSqlVal(value, "'");
    }

    @Nullable
    private static String toSqlVal(Object value, String wrapper) {
        if (value instanceof Collection<?> collection) {
            var subWrap = wrapper.equals("'") ? "\"" : "'";
            return wrapper + "{" + collection.stream()
                    .map(subVal -> toSqlVal(subVal, subWrap))
                    .collect(joining(",")) + "}" + wrapper;
        } else if (value instanceof String) {
            return wrapper + value + wrapper;
        } else if (value instanceof Temporal) {
            return wrapper + value + wrapper;
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value == null) {
            return null;
        }
        throw new IllegalArgumentException("Unsupported type: " + value.getClass());
    }

    @NotNull
    private static <T extends Typed<?>> Map<? extends T, Alias> toAliases(String tableAlias, Collection<? extends T> values) {
        return values.stream().map(t -> toAlias(tableAlias, t))
                .collect(toMap(Entry::getKey, Entry::getValue, (l, r) -> l, LinkedHashMap::new));
    }

    private static <T extends Typed<?>> Entry<T, Alias> toAlias(String tableAlias, T t) {
        return entry(t, new Alias(tableAlias + "." + t.name(), tableAlias + "_" + t.name()));
    }

    @NotNull
    private static SelectAllById getSelectAll(String taskTableAlias, String taskTagTableAlias, String condition) {
        var taskColumnAliases = toAliases(taskTableAlias, TaskColumn.values());
        var taskTagsColumnAlias = toAlias(taskTagTableAlias, TaskTagColumn.tag);
        var selectColumns = concat(taskColumnAliases.entrySet().stream(), Stream.of(taskTagsColumnAlias))
                .map(Entry::getValue)
                .map(alias -> alias.colRef + " \"" + alias.name + "\"").collect(joining(","));
        var sqlJoin = "select " + selectColumns + " from " + TaskImpl.TABLE_NAME_TASK + " t " +
                "left join " + TaskImpl.TABLE_NAME_TASK_TAG + " tt on " +
                "t." + TaskColumn.id.name() + " = tt." + TaskTagColumn.task_id.name() + " " + condition;
        return new SelectAllById(taskColumnAliases, taskTagsColumnAlias.getValue(), sqlJoin);
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
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            var sqlParts = getSelectAll("t", "tt", "where  t." + TaskColumn.id.name() + " = "+toSqlVal(id));
            try (var resultSet = statement.executeQuery(sqlParts.sqlJoin)) {
                var tasks = toTaskImpList(resultSet, sqlParts.taskColumnAliases, sqlParts.taskTagsColumnAlias);
                return !tasks.isEmpty() ? tasks.getFirst() : null;
            }
        }
    }

    @Override
    @SneakyThrows
    public TaskImpl store(TaskImpl entity, String id) {
        try (var connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                try (var statement = connection.createStatement()) {
                    var taskUpsert = queryTemplates.SQL_TASK_UPSERT;
                    for (var placeholder : queryTemplates.SQL_TASK_UPSERT_PLACEHOLDERS) {
                        taskUpsert = taskUpsert.replace(placeholder.placeholder(), toSqlVal(placeholder.column().get(entity)));
                    }
                    statement.addBatch(taskUpsert);

                    var tags = ofNullable(entity.getTags()).orElse(Set.of());
                    var deleteUnusedForTaskId = queryTemplates.SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID;
                    deleteUnusedForTaskId = deleteUnusedForTaskId.replace("$1", toSqlVal(id));
                    deleteUnusedForTaskId = deleteUnusedForTaskId.replace("$2", toSqlVal(tags));
                    statement.addBatch(deleteUnusedForTaskId);

                    for (var tag : tags) {
                        var tagInsert = queryTemplates.SQL_TASK_TAG_INSERT;
                        var placeholders = query.SQL_TASK_TAG_INSERT_PLACEHOLDERS;
                        tagInsert = tagInsert.replace("$" + placeholders.get(TaskTagColumn.task_id), toSqlVal(id));
                        tagInsert = tagInsert.replace("$" + placeholders.get(TaskTagColumn.tag), toSqlVal(tag));
                        statement.addBatch(tagInsert);
                    }

                    statement.executeBatch();
                }
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
                var sql = query.SQL_TASK_DELETE_BY_ID + ";" + query.SQL_TASK_TAG_DELETE_BY_TASK_ID + ";";
                int deletedCount;
                try (var statement = connection.prepareStatement(sql)) {
                    statement.setString(1, id);
                    statement.setString(2, id);
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

    record Alias(String colRef, String name) {
    }

    private record SelectAllById(Map<? extends TaskColumn<?>, Alias> taskColumnAliases,
                                 Alias taskTagsColumnAlias,
                                 String sqlJoin) {
    }
}
