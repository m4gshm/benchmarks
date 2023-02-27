package gen

import (
	"context"
	"errors"

	"gorm.io/gen"
	"gorm.io/gorm"

	"benchmark/rest/storage"
	"benchmark/rest/storage/gorm/gen/query"
	"benchmark/rest/storage/gorm/model"
)

func NewRepository(db *gorm.DB, opts ...gen.DOOption) *Repository {
	return &Repository{db: db, opts: opts}
}

var _ storage.API[*model.Task, string] = (*Repository)(nil)

type Repository struct {
	db   *gorm.DB
	opts []gen.DOOption
}

func (r *Repository) query() *query.Query {
	return newQuery(r.db, r.opts)
}

func newQuery(db *gorm.DB, opts []gen.DOOption) *query.Query {
	return query.Use(db, opts...)
}

// Delete implements storage.API
func (r *Repository) Delete(ctx context.Context, id string) (deleted bool, err error) {
	q := r.query().WriteDB()
	err = q.Transaction(func(tx *query.Query) error {
		if _, err := tx.TaskTag.WithContext(ctx).Where(q.TaskTag.TaskID.Eq(id)).Delete(); err != nil {
			return err
		}
		result, err := tx.Task.WithContext(ctx).Where(q.Task.ID.Eq(id)).Delete()
		if err != nil {
			return err
		}
		deleted = result.RowsAffected > 0
		return nil
	})
	return
}

// Get implements storage.API
func (r *Repository) Get(ctx context.Context, id string) (*model.Task, bool, error) {
	q := r.query().ReadDB()
	t, err := q.Task.WithContext(ctx).Preload(q.Task.Tags).Where(q.Task.ID.Eq(id)).First()
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, false, nil
	}
	return t, t != nil, err
}

// List implements storage.API
func (r *Repository) List(ctx context.Context) ([]*model.Task, error) {
	q := r.query().ReadDB()
	return q.Task.WithContext(ctx).Preload(q.Task.Tags).Find()
}

// Store implements storage.API
func (r *Repository) Store(ctx context.Context, t *model.Task) (*model.Task, error) {
	q := r.query().WriteDB()
	err := q.Transaction(func(tx *query.Query) error {
		if _, err := tx.TaskTag.WithContext(ctx).
			Where(tx.TaskTag.TaskID.Eq(t.ID), tx.TaskTag.Tag.NotIn(model.ConvertTagsGormToDto(t.Tags)...)).
			Delete(); err != nil {
			return err
		} else if err := tx.Task.WithContext(ctx).Save(t); err != nil {
			return err
		} else if err := tx.TaskTag.WithContext(ctx).Save(t.Tags...); err != nil {
			return err
		}
		return nil
	})
	return t, err
}
