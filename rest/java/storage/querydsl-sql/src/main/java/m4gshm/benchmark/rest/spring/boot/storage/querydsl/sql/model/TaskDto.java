package m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model;

import javax.annotation.processing.Generated;
import com.querydsl.sql.Column;

/**
 * TaskDto is a Querydsl bean type
 */
@Generated("com.querydsl.codegen.BeanSerializer")
public class TaskDto {

    @Column("deadline")
    private java.time.LocalDateTime deadline;

    @Column("id")
    private String id;

    @Column("text")
    private String text;

    public java.time.LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(java.time.LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}

