package postgres

import (
	"benchmark/rest/storage"
	"context"
	"errors"
	"fmt"

	"gorm.io/gorm"
)

func NewRepository[T any, ID any](db *gorm.DB) *Repository[T, ID] {
	var entity T
	return &Repository[T, ID]{db: db, et: entity}
}

type Repository[T any, ID any] struct {
	db *gorm.DB
	et T
}

// Delete implements storage.API
func (r *Repository[T, ID]) Delete(ctx context.Context, id ID) (bool, error) {
	var entity T
	tx := r.db.Delete(&entity, id)
	return tx.Error == nil, tx.Error

}

// Get implements storage.API
func (r *Repository[T, ID]) Get(ctx context.Context, id ID) (T, bool, error) {
	var entity T
	tx := r.db.Take(&entity, fmt.Sprintf("id = '%v'", id))
	if err := tx.Error; err == nil {
		return entity, true, nil
	} else if notFound := errors.Is(tx.Error, gorm.ErrRecordNotFound); notFound {
		return entity, false, nil
	} else {
		return entity, false, err
	}

}

// List implements storage.API
func (r *Repository[T, ID]) List(context.Context) ([]T, error) {
	var entities []T
	tx := r.db.Find(&entities)
	return entities, tx.Error

}

// Store implements storage.API
func (r *Repository[T, ID]) Store(ctx context.Context, entity T) (T, error) {
	tx := r.db.Create(entity)
	return entity, tx.Error
}

// Update implements storage.API
func (r *Repository[T, ID]) Update(ctx context.Context, entity T) (T, error) {
	tx := r.db.Save(entity)
	return entity, tx.Error
}

var _ storage.API[any, any] = (*Repository[any, any])(nil)
