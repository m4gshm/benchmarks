package gorm

import (
	"benchmark/rest/storage"
	"context"
	"errors"
	"runtime/trace"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

const (
	idColumnName = "id"
	storage_pref = "GormStorage."
)

func NewRepository[T any, ID any](db *gorm.DB, save func(t T, db *gorm.DB) error, deleteByID func(db *gorm.DB, id ID) (bool, error)) *Repository[T, ID] {
	return &Repository[T, ID]{db: db, idCol: getIdColName(*new(T)), save: save, deleteByID: deleteByID}
}

type Repository[T any, ID any] struct {
	db         *gorm.DB
	idCol      string
	save       func(t T, db *gorm.DB) error
	deleteByID func(db *gorm.DB, id ID) (bool, error)
}

var _ storage.API[any, any] = (*Repository[any, any])(nil)

// Delete implements storage.API
func (r *Repository[T, ID]) Delete(ctx context.Context, id ID) (bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Delete")
	defer t.End()

	if deleteByID := r.deleteByID; deleteByID != nil {
		return deleteByID(r.db, id)
	}

	var entity T
	tx := r.db.Session(&gorm.Session{FullSaveAssociations: true}).Where(r.idCol, id).Delete(&entity)
	return tx.RowsAffected > 0, tx.Error
}

// Get implements storage.API
func (r *Repository[T, ID]) Get(ctx context.Context, id ID) (T, bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Get")
	defer t.End()

	var entity T
	tx := r.db.Joins(clause.Associations).Where(r.idCol, id).Take(&entity)
	if err := tx.Error; err == nil {
		return entity, true, nil
	} else if notFound := errors.Is(err, gorm.ErrRecordNotFound); notFound {
		return entity, false, nil
	} else {
		return entity, false, err
	}
}

// List implements storage.API
func (r *Repository[T, ID]) List(ctx context.Context) ([]T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"List")
	defer t.End()

	var entities []T
	tx := r.db.Joins(clause.Associations).Find(&entities)
	return entities, tx.Error
}

// Store implements storage.API
func (r *Repository[T, ID]) Store(ctx context.Context, entity T) (T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Store")
	defer t.End()

	if save := r.save; save != nil {
		return entity, save(entity, r.db)
	}

	return entity, r.db.Transaction(func(tx *gorm.DB) error {
		return DefaultSave(tx, entity).Error
	})
}

func getIdColName[T any](entity T) string {
	idCol := idColumnName
	if idAware, ok := any(entity).(IDColNameAware); ok {
		idCol = idAware.IDColName()
	}
	return idCol
}
