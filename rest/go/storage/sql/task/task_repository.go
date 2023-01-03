package task

import (
	"context"
	"database/sql"
	"fmt"
	"strings"

	_ "github.com/jackc/pgx/v5"
	"github.com/m4gshm/gollections/map_/group"
	"github.com/m4gshm/gollections/slice"

	"benchmark/rest/model"
	storsql "benchmark/rest/storage/sql"
)

//go:generate fieldr -type Task -in ../../../model/task.go
//go:fieldr enum-const -export -val "field.name" -name "{{(join struct.name \"Field\" field.name)}}"
//go:fieldr enum-const -export -val "field.type.Type" -name "{{(join struct.name \"FieldType\" field.name)}}" -list TaskColumnTypes -exclude Tags
//go:fieldr enum-const -export -type "TaskColumn" -val "low field.name" -name "{{(join  struct.name \"Column\" field.name)}}" -list "TaskColumns" -list . -ref-access . -exclude Tags -check-unique-val

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
func Delete(ctx context.Context, db *sql.DB, id string) (bool, error) {
	return storsql.DoTransactional(ctx, db, func(ctx context.Context, db storsql.DB) (bool, error) {
		if _, err := db.ExecContext(ctx, sqlTaskTag.deleteByTaskId, id); err != nil {
			return false, err
		} else if cmdTag, err := db.ExecContext(ctx, sqlTask.deleteById, id); err != nil {
			return false, err
		} else if rowsA, err := cmdTag.RowsAffected(); err != nil {
			return false, err
		} else {
			return rowsA > 0, nil
		}
	})
}

// Get
func Get(ctx context.Context, db *sql.DB, id string) (*model.Task, bool, error) {
	entity, err := storsql.DoNoTransactional(ctx, db, func(ctx context.Context, db storsql.DB) (*model.Task, error) {
		if rows, err := db.QueryContext(ctx, sqlTask.selectById, id); err != nil {
			return nil, err
		} else {
			defer rows.Close()
			if !rows.Next() {
				return nil, nil
			} else if entity, err := extractTaskEntity(ctx, rows, db); err != nil {
				return nil, err
			} else {
				rows.Close()
				if tags, err := extractTaskTags(ctx, db, entity.ID); err != nil {
					return nil, err
				} else {
					entity.Tags = tags
				}
				return entity, nil
			}
		}
	})
	return entity, entity != nil, err
}

// List
func List(ctx context.Context, db *sql.DB) ([]*model.Task, error) {
	return storsql.DoNoTransactional(ctx, db, func(ctx context.Context, db storsql.DB) ([]*model.Task, error) {
		rows, err := db.QueryContext(ctx, sqlTask.selectAll)
		if err != nil {
			return nil, err
		}
		defer rows.Close()
		entities, err := slice.OfLoop(rows, (*sql.Rows).Next, func(rows *sql.Rows) (*model.Task, error) {
			return extractTaskEntity(ctx, rows, db)
		})
		if err != nil {
			return nil, err
		}
		rows.Close()

		ids := slice.Convert(entities, (*model.Task).GetId)
		taskTags, err := extractTasksTags(ctx, db, ids)
		if err != nil {
			return nil, err
		}

		for _, entity := range entities {
			entity.Tags = taskTags[entity.GetId()]
		}
		return entities, nil
	})
}

// Store
func Store(ctx context.Context, db *sql.DB, entity *model.Task) (*model.Task, error) {
	if _, err := storsql.DoTransactional(ctx, db, func(ctx context.Context, db storsql.DB) (any, error) {
		if _, err := db.ExecContext(ctx, sqlTask.upsertById, taskTable.fieldReferences(entity)...); err != nil {
			return nil, err
		} else if _, err := db.ExecContext(ctx, sqlTaskTag.deleteByTaskIdAndUnusedTags, entity.ID, entity.Tags); err != nil {
			return nil, err
		} else {
			for _, tag := range entity.Tags {
				if _, err := db.ExecContext(ctx, sqlTaskTag.insert, entity.ID, tag); err != nil {
					return nil, err
				}
			}
		}
		return nil, nil
	}); err != nil {
		return nil, err
	}
	return entity, nil
}

func extractTaskEntity(ctx context.Context, rows *sql.Rows, db storsql.DB) (*model.Task, error) {
	entity := &model.Task{}
	return entity, rows.Scan(&entity.ID, &entity.Text, &entity.Deadline)
}

func extractTaskTags(ctx context.Context, db storsql.DB, id string) ([]string, error) {
	rows, err := db.QueryContext(ctx, sqlTaskTag.selectByTaskId, id)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	return slice.OfLoop(rows, (*sql.Rows).Next, func(tagRows *sql.Rows) (string, error) {
		var tag string
		return tag, tagRows.Scan(&tag)
	})
}

func extractTasksTags(ctx context.Context, db storsql.DB, id []string) (map[string][]string, error) {
	rows, err := db.QueryContext(ctx, sqlTaskTag.selectByTaskIds, id)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	return group.OfLoop(rows, (*sql.Rows).Next, func(tagRows *sql.Rows) (string, string, error) {
		var taskId, tag string
		return taskId, tag, tagRows.Scan(&taskId, &tag)
	})
}
