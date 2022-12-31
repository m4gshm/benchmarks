package sql

import (
	"context"
	"database/sql"
)

func DoTransactional[T any](ctx context.Context, db *sql.DB, routine func(ctx context.Context, tx DB) (T, error)) (T, error) {
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

func DoNoTransactional[T any](ctx context.Context, db *sql.DB, routine func(ctx context.Context, tx DB) (T, error)) (T, error) {
	return routine(ctx, db)
}

type DB interface {
	QueryContext(ctx context.Context, sql string, args ...any) (*sql.Rows, error)
	ExecContext(ctx context.Context, sql string, args ...any) (sql.Result, error)
}

var _ DB = (*sql.DB)(nil)
var _ DB = (*sql.Tx)(nil)
