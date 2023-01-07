package sql

import (
	"context"
	"database/sql"

	"github.com/jackc/pgx"
)

func DoTx[Tx any, R any](
	ctx context.Context, 
	do func(context.Context, Tx) (R, error),
	begin func(context.Context) (Tx, error),
	commit func(context.Context, Tx) error,
	rollback func(context.Context, Tx) error,
) (R, error) {
	if tx, err := begin(ctx); err != nil {
		var no R
		return no, err
	} else if t, err := do(ctx, tx); err != nil {
		_ = rollback(ctx, tx)
		return t, err
	} else {
		return t, commit(ctx, tx)
	}
}

type Rows interface {
	Next() bool
	Scan(dest ...any) error
}

var _ Rows = (*sql.Rows)(nil)
var _ Rows = (*pgx.Rows)(nil)
