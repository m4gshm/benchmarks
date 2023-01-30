package m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.dml.SQLMergeClause;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;

public class PostgresUpsertClause extends SQLMergeClause {
    public PostgresUpsertClause(Connection connection, Configuration configuration, RelationalPathBase<?> entity) {
        super(connection, configuration, entity);
    }

    @Override
    protected SQLSerializer createSerializer() {
        var serializer = new SQLSerializer(this.configuration, true) {

            @Override
            protected void serializeForMerge(
                    QueryMetadata metadata, RelationalPath<?> entity, List<Path<?>> keys,
                    List<Path<?>> columns, List<Expression<?>> values, @Nullable SubQueryExpression<?> subQuery
            ) {
                super.serializeForInsert(metadata, entity, columns, values, subQuery);
                if (!keys.isEmpty()) {
                    append(" on conflict (");
                    skipParent = true;
                    handle(COMMA, keys);
                    skipParent = false;
                    append(") do ");
                }
                var updates = new LinkedHashMap<Path<?>, Expression<?>>();
                for (int i = 0; i < columns.size(); i++) {
                    var column = columns.get(i);
                    var expression = values.get(i);
                    updates.put(column, expression);
                }
                super.serializeForUpdate(
                        metadata,
                        new RelationalPathBase<>(entity.getType(), entity.getMetadata(), "", ""),
                        updates);
            }
        };
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }
}
