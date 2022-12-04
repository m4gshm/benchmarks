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

func NewRepository[T any, ID any](db *gorm.DB) *Repository[T, ID] {
	return &Repository[T, ID]{db: db}
}

type Repository[T any, ID any] struct {
	db *gorm.DB
}

var _ storage.API[any, any] = (*Repository[any, any])(nil)

// Delete implements storage.API
func (r *Repository[T, ID]) Delete(ctx context.Context, id ID) (bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Delete")
	defer t.End()

	var entity T
	if ss, ok := any(entity).(SelfDelete[ID]); ok {
		return ss.DeleteByID(ctx, r.db, id)
	}

	idCol := getIdColName(entity)
	tx := r.db.Session(&gorm.Session{FullSaveAssociations: true}).Where(idCol, id).Delete(&entity)
	return tx.RowsAffected > 0, tx.Error
}

// Get implements storage.API
func (r *Repository[T, ID]) Get(ctx context.Context, id ID) (T, bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Get")
	defer t.End()

	var entity T
	idCol := getIdColName(entity)
	tx := r.db.Preload(clause.Associations).Where(idCol, id).Take(&entity)
	if err := tx.Error; err == nil {
		return entity, true, nil
	} else if notFound := errors.Is(tx.Error, gorm.ErrRecordNotFound); notFound {
		return entity, false, nil
	} else {
		return entity, false, err
	}
}

func getIdColName[T any](entity T) string {
	idCol := idColumnName
	if idAware, ok := any(entity).(IDColNameAware); ok {
		idCol = idAware.IDColName()
	}
	return idCol
}

// List implements storage.API
func (r *Repository[T, ID]) List(ctx context.Context) ([]T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"List")
	defer t.End()

	var entities []T
	tx := r.db.Preload(clause.Associations).Find(&entities)
	return entities, tx.Error
}

// Store implements storage.API
func (r *Repository[T, ID]) Store(ctx context.Context, entity T) (T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Store")
	defer t.End()
	if ss, ok := any(entity).(SelfSave); ok {
		err := ss.Save(ctx, r.db)
		return entity, err
	}

	return entity, r.db.Transaction(func(tx *gorm.DB) error {
		return DefaultSave(tx, entity).Error
	})
}
