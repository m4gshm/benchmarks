package sql

import (
	"benchmark/rest/model"
	"benchmark/rest/storage"
	"context"
	"runtime/trace"

	"github.com/jackc/pgx/v5"
	"github.com/m4gshm/gollections/slice"
)

func NewTaskRepository(db *pgx.Conn) storage.API[*model.Task, string] {
	return &TaskRepository{db: db}
}

type TaskRepository struct {
	db *pgx.Conn
}

var _ storage.API[*model.Task, string] = (*TaskRepository)(nil)

const (
	TABLE_TASK     = "task"
	TABLE_TASK_TAG = "task_tag"
	COLUMN_ID      = "id"
)

// Delete implements storage.API
func (r *TaskRepository) Delete(ctx context.Context, id string) (bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Delete")
	defer t.End()

	return doTransactional(ctx, r.db, func(ctx context.Context, tx pgx.Tx) (bool, error) {
		if _, err := tx.Exec(ctx, "delete from "+TABLE_TASK_TAG+" where task_id=$1", id); err != nil {
			return false, err
		} else if cmdTag, err := tx.Exec(ctx, "delete from "+TABLE_TASK+" where id=$1", id); err != nil {
			return false, err
		} else {
			rowsA := cmdTag.RowsAffected()
			return rowsA > 0, nil
		}
	})
}

// Get implements storage.API
func (r *TaskRepository) Get(ctx context.Context, id string) (*model.Task, bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Get")
	defer t.End()

	entity, err := doTransactional(ctx, r.db, func(ctx context.Context, tx pgx.Tx) (*model.Task, error) {
		if rows, err := tx.Query(ctx, "select id,text,deadline from "+TABLE_TASK+" where id=$1", id); err != nil {
			return nil, err
		} else {
			defer rows.Close()
			if !rows.Next() {
				return nil, nil
			} else if entity, err := extractTaskEntity(ctx, rows, tx); err != nil {
				return nil, err
			} else {
				rows.Close()
				if tags, err := extractTaskTags(ctx, tx, entity.ID); err != nil {
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

	return doTransactional(ctx, r.db, func(ctx context.Context, tx pgx.Tx) ([]*model.Task, error) {
		rows, err := r.db.Query(ctx, "select id,text,deadline from "+TABLE_TASK)
		if err != nil {
			return nil, err
		}
		defer rows.Close()
		entities, err := slice.OfLoop(rows, pgx.Rows.Next, func(rows pgx.Rows) (*model.Task, error) {
			return extractTaskEntity(ctx, rows, r.db)
		})
		if err != nil {
			return nil, err
		}
		rows.Close()
		for _, entity := range entities {
			if tags, err := extractTaskTags(ctx, tx, entity.ID); err != nil {
				return nil, err
			} else {
				entity.Tags = tags
			}
		}
		return entities, nil
	})
}

func extractTaskEntity(ctx context.Context, rows pgx.Rows, db Queryable) (*model.Task, error) {
	entity := &model.Task{}
	return entity, rows.Scan(&entity.ID, &entity.Text, &entity.Deadline)
}

func extractTaskTags(ctx context.Context, db Queryable, id string) ([]string, error) {
	rows, err := db.Query(ctx, "select tag from "+TABLE_TASK_TAG+" where task_id=$1", id)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	return slice.OfLoop(rows, pgx.Rows.Next, func(tagRows pgx.Rows) (string, error) {
		var tag string
		return tag, tagRows.Scan(&tag)
	})
}

// Store implements storage.API
func (r *TaskRepository) Store(ctx context.Context, entity *model.Task) (*model.Task, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Store")
	defer t.End()

	if _, err := doTransactional(ctx, r.db, func(ctx context.Context, tx pgx.Tx) (any, error) {
		if _, err := tx.Exec(
			ctx, "insert into "+TABLE_NAME+" (id,text,deadline) values($1,$2,$3) on conflict (id) do update set text=$2, deadline=$3",
			entity.ID, entity.Text, entity.Deadline,
		); err != nil {
			return nil, err
		} else if _, err := tx.Exec(ctx, "delete from "+TABLE_TASK_TAG+" where task_id=$1", entity.ID); err != nil {
			return nil, err
		} else {
			sqlInsert := "insert into " + TABLE_TASK_TAG + " (task_id, tag) values ($1,$2) on conflict do nothing"
			for _, tag := range entity.Tags {
				if _, err := tx.Exec(ctx, sqlInsert, entity.ID, tag); err != nil {
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

func doTransactional[T any](ctx context.Context, db *pgx.Conn, routine func(ctx context.Context, tx pgx.Tx) (T, error)) (T, error) {
	if tx, err := db.Begin(ctx); err != nil {
		var no T
		return no, err
	} else if t, err := routine(ctx, tx); err != nil {
		tx.Rollback(ctx)
		return t, err
	} else {
		tx.Commit(ctx)
		return t, nil
	}
}

type Queryable interface {
	Query(ctx context.Context, sql string, args ...any) (pgx.Rows, error)
}

var _ Queryable = (*pgx.Conn)(nil)
var _ Queryable = (pgx.Tx)(nil)
