package m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTaskTag is a Querydsl query type for TaskTagDto
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTaskTag extends com.querydsl.sql.RelationalPathBase<TaskTagDto> {

    private static final long serialVersionUID = 369935719;

    public static final QTaskTag taskTag = new QTaskTag("task_tag");

    public final StringPath tag = createString("tag");

    public final StringPath taskId = createString("taskId");

    public final com.querydsl.sql.PrimaryKey<TaskTagDto> taskTagPkey = createPrimaryKey(taskId, tag);

    public final com.querydsl.sql.ForeignKey<TaskDto> taskTagsFk = createForeignKey(taskId, "id");

    public QTaskTag(String variable) {
        super(TaskTagDto.class, forVariable(variable), "public", "task_tag");
        addMetadata();
    }

    public QTaskTag(String variable, String schema, String table) {
        super(TaskTagDto.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTaskTag(String variable, String schema) {
        super(TaskTagDto.class, forVariable(variable), schema, "task_tag");
        addMetadata();
    }

    public QTaskTag(Path<? extends TaskTagDto> path) {
        super(path.getType(), path.getMetadata(), "public", "task_tag");
        addMetadata();
    }

    public QTaskTag(PathMetadata metadata) {
        super(TaskTagDto.class, metadata, "public", "task_tag");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(tag, ColumnMetadata.named("tag").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(taskId, ColumnMetadata.named("task_id").withIndex(1).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

