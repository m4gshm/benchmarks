package sql

import (
	"benchmark/rest/storage"
	"benchmark/rest/storage/sql/model"
	"context"
	"database/sql"
	"runtime/trace"
	"strings"
)

const TABLE_NAME = "task"

func NewRepository[T any, ID any](db *sql.DB, allColumns []string, idColumn string, refs func(T) []any) *Repository[T, ID] {
	return &Repository[T, ID]{
		db: db,
		columns: struct {
			all []string
			id  string
		}{all: allColumns, id: idColumn},
		refs: refs,
	}
}

type Repository[T any, ID any] struct {
	db      *sql.DB
	refs    func(T) []any
	columns struct {
		all []string
		id  string
	}
}

var _ storage.API[any, any] = (*Repository[any, any])(nil)

var storage_pref = "SqlStorage."

// Delete implements storage.API
func (r *Repository[T, ID]) Delete(ctx context.Context, id ID) (bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Delete")
	defer t.End()

	panic("unimplemented")
}

// Get implements storage.API
func (r *Repository[T, ID]) Get(ctx context.Context, id ID) (T, bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Get")
	defer t.End()

	cols := strings.Join(r.columns.all, ",")

	rows, err := r.db.Query("SELECT "+cols+" FROM "+TABLE_NAME+" WHERE "+r.columns.id+" = $1", id)
	if err != nil {
		var no T
		return no, false, err
	}
	found := rows.Next()
	if !found {
		var no T
		return no, false, nil
	}
	var entity T
	err = rows.Scan(r.refs(entity)...)
	return entity, true, err
}

// List implements storage.API
func (r *Repository[T, ID]) List(ctx context.Context) ([]T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"List")
	defer t.End()

	panic("unimplemented")
}

// Store implements storage.API
func (r *Repository[T, ID]) Store(ctx context.Context, entity T) (T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Store")
	defer t.End()
	panic("unimplemented")
}
