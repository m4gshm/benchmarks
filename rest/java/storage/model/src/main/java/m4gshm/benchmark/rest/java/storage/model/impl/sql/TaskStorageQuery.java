package m4gshm.benchmark.rest.java.storage.model.impl.sql;

import m4gshm.benchmark.rest.java.storage.model.impl.TaskImplMeta.TaskColumn;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskTagImplMeta.TaskTagColumn;
import m4gshm.benchmark.rest.java.storage.sql.SqlUtils.ModifyDataSqlParts.ColumnPlaceholder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl.TABLE_NAME_TASK;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl.TABLE_NAME_TASK_TAG;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskImplMeta.TaskColumn.id;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskTagImplMeta.TaskTagColumn.tag;
import static m4gshm.benchmark.rest.java.storage.model.impl.TaskTagImplMeta.TaskTagColumn.task_id;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.ModifyDataSqlParts.newModifyDataSqlParts;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.deleteBy;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.deleteByAndExcludeBy;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.insert;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.selectAll;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.selectBy;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.selectByAny;
import static m4gshm.benchmark.rest.java.storage.sql.SqlUtils.upsert;

public class TaskStorageQuery {
    public final String SQL_TASK_SELECT_ALL;
    public final String SQL_TASK_UPSERT;
    public final List<ColumnPlaceholder<TaskColumn<?>>> SQL_TASK_UPSERT_PLACEHOLDERS;
    public final Map<TaskTagColumn<?>, Integer> SQL_TASK_TAG_INSERT_PLACEHOLDERS;
    public final String SQL_TASK_TAG_INSERT;

    public final String SQL_TASK_SELECT_BY_ID;
    public final String SQL_TASK_DELETE_BY_ID;
    public final String SQL_TASK_TAG_SELECT_BY_TASK_ID;
    public final String SQL_TASK_TAG_SELECT_BY_TASK_IDS;
    public final String SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID;
    public final String SQL_TASK_TAG_DELETE_BY_TASK_ID;

    public TaskStorageQuery(IntFunction<String> placeholder, boolean appendUpsertColumns) {
        var taskTagDataParts = newModifyDataSqlParts(TaskTagColumn.values(), placeholder);
        SQL_TASK_TAG_INSERT = insert(TABLE_NAME_TASK_TAG, taskTagDataParts) + " ON CONFLICT DO NOTHING";

        var taskTagInsertPlaceholders = new HashMap<TaskTagColumn<?>, Integer>();
        var columnInsertPlaceholders = taskTagDataParts.columnInsertPlaceholders();
        for (var i = 0; i < columnInsertPlaceholders.size(); i++) {
            taskTagInsertPlaceholders.put(columnInsertPlaceholders.get(i).column(), i + 1);
        }
        SQL_TASK_TAG_INSERT_PLACEHOLDERS = unmodifiableMap(taskTagInsertPlaceholders);

        var taskDataParts = newModifyDataSqlParts(TaskColumn.values(), placeholder);
        SQL_TASK_UPSERT = upsert(TABLE_NAME_TASK, taskDataParts);

        var insertPlaceholders = taskDataParts.columnInsertPlaceholders();
        var upsertPlaceholders = taskDataParts.columnUpdatePlaceholders();

        SQL_TASK_UPSERT_PLACEHOLDERS = (appendUpsertColumns
                ? Stream.concat(insertPlaceholders.stream(), upsertPlaceholders.stream())
                : insertPlaceholders.stream()
        ).toList();
        SQL_TASK_SELECT_ALL = selectAll(TABLE_NAME_TASK, TaskColumn.values());
        SQL_TASK_SELECT_BY_ID = selectBy(TABLE_NAME_TASK, TaskColumn.values(), id, placeholder);
        SQL_TASK_DELETE_BY_ID = deleteBy(TABLE_NAME_TASK, id, placeholder);
        SQL_TASK_TAG_SELECT_BY_TASK_ID = selectBy(TABLE_NAME_TASK_TAG, TaskTagColumn.values(), task_id, placeholder);
        SQL_TASK_TAG_SELECT_BY_TASK_IDS = selectByAny(TABLE_NAME_TASK_TAG, TaskTagColumn.values(), task_id, placeholder);
        SQL_TASK_TAG_DELETE_UNUSED_FOR_TASK_ID = deleteByAndExcludeBy(TABLE_NAME_TASK_TAG, task_id, tag, placeholder);
        SQL_TASK_TAG_DELETE_BY_TASK_ID = deleteBy(TABLE_NAME_TASK_TAG, task_id, placeholder);
    }
}
