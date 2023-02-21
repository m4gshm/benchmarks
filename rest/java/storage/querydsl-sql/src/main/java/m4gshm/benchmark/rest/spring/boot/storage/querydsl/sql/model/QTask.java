package m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTask is a Querydsl query type for TaskDto
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTask extends com.querydsl.sql.RelationalPathBase<TaskDto> {

    private static final long serialVersionUID = 948924317;

    public static final QTask task = new QTask("task");

    public final DateTimePath<java.time.LocalDateTime> deadline = createDateTime("deadline", java.time.LocalDateTime.class);

    public final StringPath id = createString("id");

    public final StringPath text = createString("text");

    public final com.querydsl.sql.PrimaryKey<TaskDto> taskPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<TaskTagDto> _taskTagsFk = createInvForeignKey(id, "task_id");

    public QTask(String variable) {
        super(TaskDto.class, forVariable(variable), "public", "task");
        addMetadata();
    }

    public QTask(String variable, String schema, String table) {
        super(TaskDto.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTask(String variable, String schema) {
        super(TaskDto.class, forVariable(variable), schema, "task");
        addMetadata();
    }

    public QTask(Path<? extends TaskDto> path) {
        super(path.getType(), path.getMetadata(), "public", "task");
        addMetadata();
    }

    public QTask(PathMetadata metadata) {
        super(TaskDto.class, metadata, "public", "task");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(deadline, ColumnMetadata.named("deadline").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(text, ColumnMetadata.named("text").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

