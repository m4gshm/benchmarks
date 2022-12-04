package sql

import (
	"benchmark/rest/storage"
	"context"
	"runtime/trace"

	"github.com/jackc/pgx/v5"
	"github.com/m4gshm/gollections/slice"
)

const TABLE_NAME = "task"

func NewRepository[T any, ID any](
	db *pgx.Conn, getById, getAll, upsertById, deleteById string, allRefs, updateRefs func(T) []any,
) *Repository[T, ID] {
	return &Repository[T, ID]{
		db: db,
		sql: struct {
			getById, getAll, upsertById, deleteById string
		}{getById, getAll, upsertById, deleteById},
		refs: struct {
			all    func(T) []any
			update func(T) []any
		}{allRefs, updateRefs},
	}
}

type Repository[T any, ID any] struct {
	db  *pgx.Conn
	sql struct {
		getById, getAll, upsertById, deleteById string
	}
	refs struct {
		all    func(T) []any
		update func(T) []any
	}
}

var _ storage.API[any, any] = (*Repository[any, any])(nil)

var storage_pref = "SqlStorage."

// Delete implements storage.API
func (r *Repository[T, ID]) Delete(ctx context.Context, id ID) (bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Delete")
	defer t.End()
	if result, err := r.db.Exec(ctx, r.sql.deleteById, id); err != nil {
		return false, err
	} else {
		rows := result.RowsAffected()
		success := rows > 0
		return success, err
	}
}

// Get implements storage.API
func (r *Repository[T, ID]) Get(ctx context.Context, id ID) (T, bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Get")
	defer t.End()
	if rows, err := r.db.Query(ctx, r.sql.getById, id); err != nil {
		return *new(T), false, err
	} else if found := rows.Next(); !found {
		return *new(T), false, nil
	} else {
		entity, err := r.toEntity(rows)
		return entity, true, err
	}
}

// List implements storage.API
func (r *Repository[T, ID]) List(ctx context.Context) ([]T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"List")
	defer t.End()

	rows, err := r.db.Query(ctx, r.sql.getAll)
	if err != nil {
		return nil, err
	}
	return slice.OfLoop(rows, pgx.Rows.Next, r.toEntity)
}

// Store implements storage.API
func (r *Repository[T, ID]) Store(ctx context.Context, entity T) (T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Store")
	defer t.End()
	result, err := r.db.Exec(ctx, r.sql.upsertById, r.refs.update(entity)...)
	_ = result
	return entity, err
}

func (r *Repository[T, ID]) toEntity(rows pgx.Rows) (entity T, err error) {
	return entity, rows.Scan(r.refs.all(entity)...)
}
