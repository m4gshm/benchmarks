package m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.IntFunction;

public class PostgresSQLSerializer extends SQLSerializer {

    private final IntFunction<String> paramFactory;

    public PostgresSQLSerializer(Configuration conf, boolean dml, boolean indexedParams) {
        super(conf, dml);
        paramFactory = indexedParams ? value -> "$" + value : value -> "?";
    }

    @NotNull
    public static PostgresSQLSerializer newPostgresSQLSerializer(Configuration configuration, boolean indexedParams, boolean dml) {
        return new PostgresSQLSerializer(configuration, dml, indexedParams);
    }

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

    @Override
    protected void serializeConstant(int parameterIndex, String constantLabel) {
        append(paramFactory.apply(parameterIndex));
    }
}
