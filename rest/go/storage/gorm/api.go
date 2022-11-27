package gorm

import (
	"context"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

type IDColNameAware interface {
	IDColName() string
}

type SelfSave interface {
	Save(ctx context.Context, db *gorm.DB) error
}

func DefaultSave[T any](db *gorm.DB, entity T) *gorm.DB {
	return db.
		Session(&gorm.Session{FullSaveAssociations: true}).
		Clauses(clause.OnConflict{UpdateAll: true}).
		Save(entity)
}
