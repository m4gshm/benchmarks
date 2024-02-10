package m4gshm.benchmark.rest.java.storage.sql;

import lombok.experimental.UtilityClass;
import meta.jpa.Column;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;

import static java.util.Collections.unmodifiableList;

@UtilityClass
public class SqlUtils {

    public static final IntFunction<String> POSTGRES_PLACEHOLDER = index -> "$" + (index + 1);
    public static final IntFunction<String> JDBC_PLACEHOLDER = index -> "?";

    public static <C extends Column<?>> String selectBy(String tableName, Collection<C> columns, C byColumn,
                                                        IntFunction<String> placeholderFactory) {
        return selectAll(tableName, columns) + " WHERE " + eq(byColumn, placeholderFactory);
    }

    public static <C extends Column<?>> String selectByAny(String tableName, Collection<C> columns, C byColumn,
                                                           IntFunction<String> placeholderFactory) {
        return selectAll(tableName, columns) + " WHERE " + any(byColumn, placeholderFactory);
    }

    public static <C extends Column<?>> String selectAll(String tableName, Collection<C> columns) {
        return "SELECT " + columns.stream().map(Column::name).reduce(commaSeparated()).orElse("*") + " FROM " + tableName;
    }

    public static <C extends Column<?>> String deleteBy(String tableName, C byColumn, IntFunction<String> placeholderFactory) {
        return "DELETE FROM " + tableName + " WHERE " + eq(byColumn, placeholderFactory);
    }

    public static <C extends Column<?>> String upsert(String tableName, ModifyDataSqlParts<C> dataParts) {
        return insert(tableName, dataParts) + " ON CONFLICT (" + dataParts.pkColumns() + ") DO UPDATE SET " + dataParts.upsertColumns();
    }

    public static <C extends Column<?>> String insert(String tableName, ModifyDataSqlParts<C> dataParts) {
        return "INSERT INTO " + tableName + "(" + dataParts.insertColumns() + ") VALUES (" + dataParts.insertPlaceholders() + ")";
    }

    @SafeVarargs
    public static <C extends Column<?>> Map<C, String> bindPlaceholder(IntFunction<String> placeholderFactory, C... columns) {
        var result = new LinkedHashMap<C, String>();
        for (int i = 0; i < columns.length; i++) {
            result.put(columns[i], placeholderFactory.apply(i));
        }
        return result;
    }

    private static BinaryOperator<String> commaSeparated() {
        return (l, r) -> l + (l.isEmpty() ? "" : ", ") + r;
    }

    private static <C extends Column<?>> String eq(C byColumn, IntFunction<String> placeholderFactory) {
        return byColumn.name() + " = " + bindPlaceholder(placeholderFactory, byColumn).get(byColumn);
    }

    private static <C extends Column<?>> String any(C byColumn, IntFunction<String> placeholderFactory) {
        return byColumn.name() + " = any(" + bindPlaceholder(placeholderFactory, byColumn).get(byColumn) + ")";
    }

    public record ModifyDataSqlParts<C extends Column<?>>(String insertColumns,
                                                             String insertPlaceholders,
                                                             String pkColumns,
                                                             String upsertColumns,
                                                             List<ColumnPlaceholder<C>> columnInsertPlaceholders,
                                                             List<ColumnPlaceholder<C>> columnUpsertPlaceholders
    ) {

        public static <C extends Column<?>> ModifyDataSqlParts<C> newModifyDataSqlParts(
                List<C> columns, IntFunction<String> placeholderFactory
        ) {
            var insertColumns = new StringBuilder();
            var insertPlaceholders = new StringBuilder();
            var pkColumns = new StringBuilder();
            var upsertColumns = new StringBuilder();
            var columnInsertPlaceholderMap = new ArrayList<ColumnPlaceholder<C>>();
            var columnUpsertPlaceholderMap = new ArrayList<ColumnPlaceholder<C>>();
            for (int i = 0; i < columns.size(); i++) {
                var placeholder = placeholderFactory.apply(i);
                var column = columns.get(i);
                var name = column.name();

                if (!insertColumns.isEmpty()) {
                    insertColumns.append(", ");
                    insertPlaceholders.append(", ");
                }

                insertColumns.append(name);
                insertPlaceholders.append(placeholder);
                columnInsertPlaceholderMap.add(new ColumnPlaceholder<>(column, placeholder, i + 1));

                if (column.pk()) {
                    if (!pkColumns.isEmpty()) {
                        pkColumns.append(", ");
                    }
                    pkColumns.append(name);
                } else {
                    if (!upsertColumns.isEmpty()) {
                        upsertColumns.append(", ");
                    }
                    upsertColumns.append(name).append("=").append(placeholder);
                    columnUpsertPlaceholderMap.add(new ColumnPlaceholder<>(column, placeholder, i + 1));
                }
            }
            return new ModifyDataSqlParts<>(insertColumns.toString(), insertPlaceholders.toString(),
                    pkColumns.toString(), upsertColumns.toString(), unmodifiableList(columnInsertPlaceholderMap),
                    unmodifiableList(columnUpsertPlaceholderMap)
            );
        }

        public record ColumnPlaceholder<C extends Column>(C column, String placeholder, int num) {

        }

    }
}
