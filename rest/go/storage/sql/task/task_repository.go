package task

import (
	"context"
	"fmt"
	"strings"

	_ "github.com/jackc/pgx/v5"
	"github.com/m4gshm/gollections/break/loop"
	"github.com/m4gshm/gollections/map_/group"
	"github.com/m4gshm/gollections/slice"

	"benchmark/rest/model"
	storsql "benchmark/rest/storage/sql"
)

//go:generate fieldr -type Task -in ../../../model/task.go
//go:fieldr enum-const -export -val "field.name" -name "join(struct.name,\"Field\",field.name)"
//go:fieldr enum-const -export -val "field.type.Type" -name "join(struct.name,\"FieldType\",field.name)" -list TaskColumnTypes -exclude Tags
//go:fieldr enum-const -export -type "TaskColumn" -val "low(field.name)" -name "join(struct.name,\"Column\",field.name)" -list "TaskColumns" -list . -ref-access . -exclude Tags -check-unique-val

const (
	TABLE_TASK     = "task"
	TABLE_TASK_TAG = "task_tag"
	TABLE_NAME     = "task"
	storage_pref   = TABLE_NAME
)

var (
	sqlTaskTag = struct{ selectByTaskId, selectByTaskIds, deleteByTaskId, deleteByTaskIdAndUnusedTags, insert string }{
		selectByTaskIds:             "select task_id,tag from " + TABLE_TASK_TAG + " where task_id=any($1)",
		selectByTaskId:              "select tag from " + TABLE_TASK_TAG + " where task_id=$1",
		deleteByTaskId:              "delete from " + TABLE_TASK_TAG + " where task_id=$1",
		deleteByTaskIdAndUnusedTags: "delete from " + TABLE_TASK_TAG + " where task_id=$1 and not tag=any($2)",
		insert:                      "insert into " + TABLE_TASK_TAG + " (task_id, tag) values ($1,$2) on conflict do nothing",
	}

	taskTable = struct {
		columns, idColumn, fieldPlaceholders, updateSetExpr string
		fieldReferences                                     func(t *model.Task) []any
	}{
		columns:           strings.Join(slice.BehaveAsStrings(TaskColumns()), ","),
		fieldPlaceholders: strings.Join(slice.ConvertIndexed(TaskColumns(), func(i int, _ TaskColumn) string { return fmt.Sprintf("$%d", i+1) }), ","),
		idColumn:          string(TaskColumnID),
		fieldReferences: func(t *model.Task) []any {
			return slice.Convert(TaskColumns(), func(c TaskColumn) any { return Ref(t, c) })
		},
		updateSetExpr: strings.Join(slice.ConvertCheckIndexed(TaskColumns(),
			func(i int, col TaskColumn) (string, bool) {
				return string(col) + fmt.Sprintf("=$%d", i+1), col != TaskColumnID
			},
		), ","),
	}

	sqlTask = struct{ selectAll, selectById, deleteById, upsertById string }{
		selectAll:  "select " + taskTable.columns + " from " + TABLE_TASK,
		selectById: "select " + taskTable.columns + " from " + TABLE_TASK + " where " + taskTable.idColumn + "=$1",
		deleteById: "delete from " + TABLE_TASK + " where " + taskTable.idColumn + "=$1",
		upsertById: "insert into " + TABLE_NAME + " (" + taskTable.columns + ") values " +
			"(" + taskTable.fieldPlaceholders + ") on conflict (" + taskTable.idColumn + ") do " + "update set " + taskTable.updateSetExpr,
	}
)

// Delete
func Delete[E any, Tx any](
	ctx context.Context,
	id string,
	begin storsql.BeginTx[Tx],
	commit storsql.EndTx[Tx],
	rollback storsql.EndTx[Tx],
	exec storsql.Exec[Tx, E],
	rowsAffected storsql.RowsAffected[E],
) (bool, error) {
	routine := func(ctx context.Context, db Tx) (bool, error) {
		if _, err := exec(ctx, db, sqlTaskTag.deleteByTaskId, id); err != nil {
			return false, err
		} else if cmdTag, err := exec(ctx, db, sqlTask.deleteById, id); err != nil {
			return false, err
		} else if rowsA, err := rowsAffected(ctx, cmdTag); err != nil {
			return false, err
		} else {
			return rowsA > 0, nil
		}
	}
	return storsql.DoTx(ctx, routine, begin, commit, rollback)
}

// Get
func Get[R storsql.Rows](ctx context.Context, id string, openRows storsql.OpenRows[R], closeRows storsql.CloseRows[R]) (*model.Task, bool, error) {
	routine := func(ctx context.Context) (*model.Task, error) {
		if rows, err := openRows(ctx, sqlTask.selectById, id); err != nil {
			return nil, err
		} else {
			defer closeRows(ctx, rows)

			entity, ok, err := loop.New(rows, R.Next, extractTaskEntity)()
			if !ok || err != nil {
				return nil, err
			}
			closeRows(ctx, rows)
			if tags, err := extractTaskTags(ctx, entity.ID, openRows, closeRows); err != nil {
				return nil, err
			} else {
				entity.Tags = tags
			}
			return entity, nil
		}
	}
	entity, err := routine(ctx)
	return entity, entity != nil, err
}

// List
func List[R storsql.Rows](ctx context.Context, openRows storsql.OpenRows[R], closeRows storsql.CloseRows[R]) ([]*model.Task, error) {
	rows, err := openRows(ctx, sqlTask.selectAll)
	if err != nil {
		return nil, err
	}
	defer closeRows(ctx, rows)

	entities, err := slice.OfLoop(rows, (R).Next, func(rows R) (*model.Task, error) { return extractTaskEntity(rows) })
	if err != nil {
		return nil, err
	}
	closeRows(ctx, rows)

	taskTags, err := extractTasksTags(ctx, slice.Convert(entities, (*model.Task).GetId), openRows, closeRows)
	if err != nil {
		return nil, err
	}

	for _, entity := range entities {
		entity.Tags = taskTags[entity.GetId()]
	}
	return entities, nil
}

// Store
func Store[R storsql.Rows, E any, Tx any](
	ctx context.Context,
	entity *model.Task,
	begin storsql.BeginTx[Tx],
	commit storsql.EndTx[Tx],
	rollback storsql.EndTx[Tx],
	exec storsql.Exec[Tx, E],
) (
	*model.Task, error,
) {
	routine := func(ctx context.Context, db Tx) (any, error) {
		if _, err := exec(ctx, db, sqlTask.upsertById, taskTable.fieldReferences(entity)...); err != nil {
			return nil, err
		} else if _, err := exec(ctx, db, sqlTaskTag.deleteByTaskIdAndUnusedTags, entity.ID, entity.Tags); err != nil {
			return nil, err
		} else {
			for _, tag := range entity.Tags {
				if _, err := exec(ctx, db, sqlTaskTag.insert, entity.ID, tag); err != nil {
					return nil, err
				}
			}
		}
		return nil, nil
	}
	if _, err := storsql.DoTx(ctx, routine, begin, commit, rollback); err != nil {
		return nil, err
	}
	return entity, nil
}

func extractTaskEntity[R storsql.Rows](rows R) (*model.Task, error) {
	entity := &model.Task{}
	return entity, rows.Scan(&entity.ID, &entity.Text, &entity.Deadline)
}

func extractTaskTags[R storsql.Rows](
	ctx context.Context, id string,
	openRows func(context.Context, string, ...any) (R, error),
	closeRows func(context.Context, R) error,
) ([]string, error) {
	rows, err := openRows(ctx, sqlTaskTag.selectByTaskId, id)
	if err != nil {
		return nil, err
	}
	defer closeRows(ctx, rows)
	return slice.OfLoop(rows, (R).Next, func(tagRows R) (tag string, err error) { return tag, tagRows.Scan(&tag) })
}

func extractTasksTags[R storsql.Rows](
	ctx context.Context, id []string,
	openRows func(context.Context, string, ...any) (R, error),
	closeRows func(context.Context, R) error,
) (map[string][]string, error) {
	rows, err := openRows(ctx, sqlTaskTag.selectByTaskIds, id)
	if err != nil {
		return nil, err
	}
	defer closeRows(ctx, rows)
	return group.OfLoop(rows, (R).Next, func(tagRows R) (taskId string, tag string, err error) {
		return taskId, tag, tagRows.Scan(&taskId, &tag)
	})
}
