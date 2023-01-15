package gen

import (
	"context"

	"gorm.io/gen"
	"gorm.io/gorm"

	"benchmark/rest/storage"
	"benchmark/rest/storage/gorm/gen/query"
	"benchmark/rest/storage/gorm/model"
)

func NewRepository(db *gorm.DB, opts ...gen.DOOption) *Repository {
	return &Repository{q: query.Use(db, opts...)}
}

var _ storage.API[*model.Task, string] = (*Repository)(nil)

type Repository struct {
	q *query.Query
}

// Delete implements storage.API
func (r *Repository) Delete(ctx context.Context, id string) (bool, error) {
	result, err := r.q.Task.WithContext(ctx).Where(query.Task.ID.Eq(id)).Delete()
	if err != nil {
		return false, err
	}
	return result.RowsAffected > 0, nil
}

// Get implements storage.API
func (r *Repository) Get(ctx context.Context, id string) (*model.Task, bool, error) {
	t, err := r.q.Task.WithContext(ctx).Where(query.Task.ID.Eq(id)).First()
	if err != nil {
		return nil, false, err
	}
	return t, t != nil, nil
}

// List implements storage.API
func (r *Repository) List(ctx context.Context) ([]*model.Task, error) {
	return r.q.Task.WithContext(ctx).Find()
}

// Store implements storage.API
func (r *Repository) Store(ctx context.Context, t *model.Task) (*model.Task, error) {
	return t, r.q.Task.WithContext(ctx).Save(t)
}
