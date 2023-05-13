package sql

import (
	"context"
	"database/sql"
	"errors"

	"github.com/jackc/pgx"
)

func DoTx[Tx any, R any](
	ctx context.Context,
	do func(context.Context, Tx) (R, error),
	begin func(context.Context) (Tx, error),
	commit func(context.Context, Tx) error,
	rollback func(context.Context, Tx) error,
) (n R, err error) {
	if tx, err := begin(ctx); err != nil {
		return n, err
	} else if y, err := do(ctx, tx); err != nil {
		rerr := rollback(ctx, tx)
		return y, errors.Join(err, rerr)
	} else {
		return y, commit(ctx, tx)
	}
}

type Rows interface {
	Next() bool
	Scan(dest ...any) error
}

var _ Rows = (*sql.Rows)(nil)
var _ Rows = (*pgx.Rows)(nil)
