package sql

import (
	"context"
	"database/sql"
	"runtime/trace"

	_ "github.com/jackc/pgx/v5"
	"github.com/m4gshm/gollections/map_/group"
	"github.com/m4gshm/gollections/slice"

	"benchmark/rest/model"
	"benchmark/rest/storage"
	sqlmodel "benchmark/rest/storage/sql/model"
)

func NewTaskRepository(db *sql.DB) storage.API[*model.Task, string] {
	return &TaskRepository{db: db}
}

type TaskRepository struct {
	db *sql.DB
}

var _ storage.API[*model.Task, string] = (*TaskRepository)(nil)

const (
	TABLE_TASK     = "task"
	TABLE_TASK_TAG = "task_tag"
)

var (
	sqlTaskTag = struct{ selectByTaskId, selectByTaskIds, deleteByTaskId, deleteByTaskIdAndUnusedTags, insert string }{
		selectByTaskIds:             "select task_id,tag from " + TABLE_TASK_TAG + " where task_id=any($1)",
		selectByTaskId:              "select tag from " + TABLE_TASK_TAG + " where task_id=$1",
		deleteByTaskId:              "delete from " + TABLE_TASK_TAG + " where task_id=$1",
		deleteByTaskIdAndUnusedTags: "delete from " + TABLE_TASK_TAG + " where task_id=$1 and not tag=any($2)",
		insert:                      "insert into " + TABLE_TASK_TAG + " (task_id, tag) values ($1,$2) on conflict do nothing",
	}

	sqlTask = struct{ selectAll, selectById, deleteById, upsertById string }{
		selectAll:  "select " + sqlmodel.SqlTaskColumns() + " from " + TABLE_TASK,
		selectById: "select " + sqlmodel.SqlTaskColumns() + " from " + TABLE_TASK + " where " + sqlmodel.TaskIdColumn() + "=$1",
		deleteById: "delete from " + TABLE_TASK + " where " + sqlmodel.TaskIdColumn() + "=$1",
		upsertById: "insert into " + TABLE_NAME + " " +
			"(" + sqlmodel.SqlTaskColumns() + ") values " +
			"(" + sqlmodel.SqlTaskColumnFieldPlaceholders() + ") on conflict (" + sqlmodel.TaskIdColumn() + ") do " +
			"update set " + sqlmodel.SqlTaskColumnUpdateExpr(),
	}
)

// Delete implements storage.API
func (r *TaskRepository) Delete(ctx context.Context, id string) (bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Delete")
	defer t.End()

	return doTransactional(ctx, r.db, func(ctx context.Context, db DB) (bool, error) {
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

// Get implements storage.API
func (r *TaskRepository) Get(ctx context.Context, id string) (*model.Task, bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Get")
	defer t.End()

	entity, err := doNoTransactional(ctx, r.db, func(ctx context.Context, db DB) (*model.Task, error) {
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

// List implements storage.API
func (r *TaskRepository) List(ctx context.Context) ([]*model.Task, error) {
	_, t := trace.NewTask(ctx, storage_pref+"List")
	defer t.End()

	return doNoTransactional(ctx, r.db, func(ctx context.Context, db DB) ([]*model.Task, error) {
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

func extractTaskEntity(ctx context.Context, rows *sql.Rows, db DB) (*model.Task, error) {
	entity := &model.Task{}
	return entity, rows.Scan(&entity.ID, &entity.Text, &entity.Deadline)
}

func extractTaskTags(ctx context.Context, db DB, id string) ([]string, error) {
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

func extractTasksTags(ctx context.Context, db DB, id []string) (map[string][]string, error) {
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

// Store implements storage.API
func (r *TaskRepository) Store(ctx context.Context, entity *model.Task) (*model.Task, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Store")
	defer t.End()

	if _, err := doTransactional(ctx, r.db, func(ctx context.Context, db DB) (any, error) {
		if _, err := db.ExecContext(ctx, sqlTask.upsertById, sqlmodel.SqlTaskColumnFieldReferences(entity)...); err != nil {
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

func doTransactional[T any](ctx context.Context, db *sql.DB, routine func(ctx context.Context, tx DB) (T, error)) (T, error) {
	if tx, err := db.BeginTx(ctx, &sql.TxOptions{}); err != nil {
		var no T
		return no, err
	} else if t, err := routine(ctx, tx); err != nil {
		tx.Rollback()
		return t, err
	} else {
		tx.Commit()
		return t, nil
	}
}

func doNoTransactional[T any](ctx context.Context, db *sql.DB, routine func(ctx context.Context, tx DB) (T, error)) (T, error) {
	return routine(ctx, db)
}

type DB interface {
	QueryContext(ctx context.Context, sql string, args ...any) (*sql.Rows, error)
	ExecContext(ctx context.Context, sql string, args ...any) (sql.Result, error)
}

var _ DB = (*sql.DB)(nil)
var _ DB = (*sql.Tx)(nil)
