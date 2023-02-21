package m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model;

import javax.annotation.processing.Generated;
import com.querydsl.sql.Column;

/**
 * TaskTagDto is a Querydsl bean type
 */
@Generated("com.querydsl.codegen.BeanSerializer")
public class TaskTagDto {

    @Column("tag")
    private String tag;

    @Column("task_id")
    private String taskId;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

}

