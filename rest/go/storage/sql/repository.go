package sql

import (
	"benchmark/rest/storage"
	"context"
	"runtime/trace"
)

func NewRepository[R Rows, E any, Tx any, T any, ID any](
	beginTx BeginTx[Tx],
	commintTx EndTx[Tx],
	rollbackTx EndTx[Tx],
	openRows OpenRows[R],
	closeRows CloseRows[R],
	exec Exec[Tx, E],
	rowsAffected RowsAffected[E],
	delete func(context.Context, ID, BeginTx[Tx], EndTx[Tx], EndTx[Tx], Exec[Tx, E], RowsAffected[E]) (bool, error),
	get func(context.Context, ID, OpenRows[R], CloseRows[R]) (T, bool, error),
	list func(context.Context, OpenRows[R], CloseRows[R]) ([]T, error),
	store func(context.Context, T, BeginTx[Tx], EndTx[Tx], EndTx[Tx], Exec[Tx, E]) (T, error),
) *Repository[R, E, Tx, T, ID] {
	return &Repository[R, E, Tx, T, ID]{
		beginTx: beginTx, rollbackTx: rollbackTx, commitTx: commintTx, openRows: openRows, closeRows: closeRows, exec: exec, rowsAffected: rowsAffected,
		delete: delete, get: get, list: list, store: store,
	}
}

type Repository[R Rows, E any, Tx any, T any, ID any] struct {
	beginTx      BeginTx[Tx]
	commitTx     EndTx[Tx]
	rollbackTx   EndTx[Tx]
	openRows     OpenRows[R]
	closeRows    CloseRows[R]
	exec         Exec[Tx, E]
	rowsAffected RowsAffected[E]

	delete func(context.Context, ID, BeginTx[Tx], EndTx[Tx], EndTx[Tx], Exec[Tx, E], RowsAffected[E]) (bool, error)
	get    func(context.Context, ID, OpenRows[R], CloseRows[R]) (T, bool, error)
	list   func(context.Context, OpenRows[R], CloseRows[R]) ([]T, error)
	store  func(context.Context, T, BeginTx[Tx], EndTx[Tx], EndTx[Tx], Exec[Tx, E]) (T, error)
}

type BeginTx[Tx any] func(context.Context) (Tx, error)
type EndTx[Tx any] func(context.Context, Tx) error

type OpenRows[R Rows] func(context.Context, string, ...any) (R, error)
type CloseRows[R Rows] func(context.Context, R) error
type Exec[Tx, E any] func(context.Context, Tx, string, ...any) (E, error)
type RowsAffected[E any] func(context.Context, E) (int64, error)

var _ storage.API[any, any] = (*Repository[Rows, any, any, any, any])(nil)

var storage_pref = "SqlStorage."

// Delete implements storage.API
func (r *Repository[R, E, Tx, T, ID]) Delete(ctx context.Context, id ID) (bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Delete")
	defer t.End()
	return r.delete(ctx, id, r.beginTx, r.commitTx, r.rollbackTx, r.exec, r.rowsAffected)

}

// Get implements storage.API
func (r *Repository[R, E, Tx, T, ID]) Get(ctx context.Context, id ID) (T, bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Get")
	defer t.End()
	return r.get(ctx, id, r.openRows, r.closeRows)
}

// List implements storage.API
func (r *Repository[R, E, Tx, T, ID]) List(ctx context.Context) ([]T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"List")
	defer t.End()
	return r.list(ctx, r.openRows, r.closeRows)
}

// Store implements storage.API
func (r *Repository[R, E, Tx, T, ID]) Store(ctx context.Context, entity T) (T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Store")
	defer t.End()
	return r.store(ctx, entity, r.beginTx, r.commitTx, r.rollbackTx, r.exec)
}
