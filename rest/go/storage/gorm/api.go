package gorm

import (
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

type IDColNameAware interface {
	IDColName() string
}

func DefaultSave[T any](db *gorm.DB, entity T) *gorm.DB {
	return db.
		Session(&gorm.Session{FullSaveAssociations: true}).
		Clauses(clause.OnConflict{UpdateAll: true}).
		Save(entity)
}
