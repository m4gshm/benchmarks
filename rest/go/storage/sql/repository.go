package sql

import (
	"benchmark/rest/storage"
	"context"
	"database/sql"
	"runtime/trace"
)

func NewRepository[T any, ID any](
	db *sql.DB,
	delete func(ctx context.Context, db *sql.DB, id ID) (bool, error),
	get func(ctx context.Context, db *sql.DB, id ID) (T, bool, error),
	list func(ctx context.Context, db *sql.DB) ([]T, error),
	store func(ctx context.Context, db *sql.DB, entity T) (T, error),
) *Repository[T, ID] {
	return &Repository[T, ID]{db: db, delete: delete, get: get, list: list, store: store}
}

type Repository[T any, ID any] struct {
	db     *sql.DB
	delete func(ctx context.Context, db *sql.DB, id ID) (bool, error)
	get    func(ctx context.Context, db *sql.DB, id ID) (T, bool, error)
	list   func(ctx context.Context, db *sql.DB) ([]T, error)
	store  func(ctx context.Context, db *sql.DB, entity T) (T, error)
}

var _ storage.API[any, any] = (*Repository[any, any])(nil)

var storage_pref = "SqlStorage."

// Delete implements storage.API
func (r *Repository[T, ID]) Delete(ctx context.Context, id ID) (bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Delete")
	defer t.End()
	return r.delete(ctx, r.db, id)

}

// Get implements storage.API
func (r *Repository[T, ID]) Get(ctx context.Context, id ID) (T, bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Get")
	defer t.End()
	return r.get(ctx, r.db, id)
}

// List implements storage.API
func (r *Repository[T, ID]) List(ctx context.Context) ([]T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"List")
	defer t.End()

	return r.list(ctx, r.db)
}

// Store implements storage.API
func (r *Repository[T, ID]) Store(ctx context.Context, entity T) (T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Store")
	defer t.End()
	return r.store(ctx, r.db, entity)
}
