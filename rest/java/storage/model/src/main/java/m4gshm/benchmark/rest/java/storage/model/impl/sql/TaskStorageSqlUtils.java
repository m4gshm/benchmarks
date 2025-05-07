package m4gshm.benchmark.rest.java.storage.model.impl.sql;

import lombok.experimental.UtilityClass;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImplMeta;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImplMeta.TaskColumn;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskTagImplMeta;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskTagImplMeta.TaskTagColumn;
import m4gshm.benchmark.rest.java.storage.sql.SqlUtils;
import m4gshm.benchmark.rest.java.storage.sql.SqlUtils.ModifyDataSqlParts.ColumnPlaceholder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl.TABLE_NAME_TASK;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl.TABLE_NAME_TASK_TAG;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.*;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.ModifyDataSqlParts.newModifyDataSqlParts;

@UtilityClass
public class TaskStorageSqlUtils {
    private static final IntFunction<String> PLACEHOLDER = JDBC_PLACEHOLDER;

    public static final String SQL_TASK_SELECT_ALL = SqlUtils.selectAll(TABLE_NAME_TASK, TaskColumn.values());
    public static final String SQL_TASK_UPSERT;
    public static final List<ColumnPlaceholder<TaskColumn<?, ?>>> SQL_TASK_UPSERT_PLACEHOLDERS;
    public static final String[] EMPTY_STRINGS = new String[0];
    public static final int[] EMPTY_INTS = new int[0];
    public static final String SQL_TASK_TAG_INSERT;
    public static final Map<TaskTagColumn<?>, Integer> SQL_TASK_TAG_INSERT_PLACEHOLDERS;
    public static final String SQL_TASK_SELECT_BY_ID = selectBy(
            TABLE_NAME_TASK, TaskColumn.values(), TaskColumn.ID, PLACEHOLDER
    );
    public static final String SQL_TASK_DELETE_BY_ID = deleteBy(TABLE_NAME_TASK, TaskColumn.ID, PLACEHOLDER);
    public static final String SQL_TASK_TAG_SELECT_BY_TASK_ID = selectBy(
            TABLE_NAME_TASK_TAG, TaskTagColumn.values(), TaskTagColumn.TASK_ID, PLACEHOLDER
    );
    public static final String SQL_TASK_TAG_SELECT_BY_TASK_IDS = SqlUtils.selectByAny(
            TABLE_NAME_TASK_TAG, TaskTagColumn.values(), TaskTagColumn.TASK_ID, PLACEHOLDER
    );
    public static final String SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID = deleteBy(
            TABLE_NAME_TASK_TAG, TaskTagColumn.TASK_ID, PLACEHOLDER
    ) + " AND NOT " + TaskTagColumn.TAG.name() + "=ANY(?)";
    public static final String SQL_TASK_TAG_DELETE_BY_TASK_ID = deleteBy(TABLE_NAME_TASK_TAG, TaskTagColumn.TASK_ID, PLACEHOLDER);

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
        SQL_TASK_UPSERT = upsert(TABLE_NAME_TASK, taskDataParts);

        var insertPlaceholders = taskDataParts.columnInsertPlaceholders();
        var upsertPlaceholders = taskDataParts.columnUpsertPlaceholders();

        SQL_TASK_UPSERT_PLACEHOLDERS = Stream.concat(insertPlaceholders.stream(), upsertPlaceholders.stream()).toList();
    }
}
