package m4gshm.benchmark.rest.java.storage.model.impl;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TaskTableHelper {
    public static final String TABLE_NAME_TASK = "task";
    public static final String TABLE_NAME_TASK_TAG = "task_tag";
    public static final String TASK_COLUMN_ID = "id";
    public static final String TASK_COLUMN_TEXT = "text";
    public static final String TASK_COLUMN_DEADLINE = "deadline";
    public static final String TASK_TAG_COLUMN_TASK_ID = "task_id";
    public static final String TASK_TAG_COLUMN_TAG = "tag";
}
