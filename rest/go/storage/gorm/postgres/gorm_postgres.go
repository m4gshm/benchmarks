package postgres

import (
	"benchmark/rest/storage"
	sgorm "benchmark/rest/storage/gorm"
	"context"
	"errors"
	"runtime/trace"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

var IDColumnName = "id"

func NewRepository[T any, ID any](db *gorm.DB) *Repository[T, ID] {
	var entity T
	return &Repository[T, ID]{db: db, et: entity}
}

type Repository[T any, ID any] struct {
	db *gorm.DB
	et T
}

var _ storage.API[any, any] = (*Repository[any, any])(nil)

var storage_pref = "GormPgStorage."

// Delete implements storage.API
func (r *Repository[T, ID]) Delete(ctx context.Context, id ID) (bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Delete")
	defer t.End()

	var entity T
	idCol := getIdColName(entity)
	tx := r.db.Where(idCol, id).Delete(&entity)
	return tx.RowsAffected > 0, tx.Error
}

// Get implements storage.API
func (r *Repository[T, ID]) Get(ctx context.Context, id ID) (T, bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Get")
	defer t.End()

	var entity T
	idCol := getIdColName(entity)
	tx := r.db.Where(idCol, id).Take(&entity)
	if err := tx.Error; err == nil {
		return entity, true, nil
	} else if notFound := errors.Is(tx.Error, gorm.ErrRecordNotFound); notFound {
		return entity, false, nil
	} else {
		return entity, false, err
	}
}

func getIdColName[T any](entity T) string {
	idCol := IDColumnName
	if idAware, ok := any(entity).(sgorm.IDColNameAware); ok {
		idCol = idAware.IDColName()
	}
	return idCol
}

// List implements storage.API
func (r *Repository[T, ID]) List(ctx context.Context) ([]T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"List")
	defer t.End()

	var entities []T
	tx := r.db.Find(&entities)
	return entities, tx.Error
}

// Store implements storage.API
func (r *Repository[T, ID]) Store(ctx context.Context, entity T) (T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Store")
	defer t.End()

	tx := r.db.Clauses(clause.OnConflict{UpdateAll: true}).Save(entity)
	return entity, tx.Error
}
