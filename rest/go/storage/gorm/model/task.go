package model

import (
	"benchmark/rest/storage"
	sgorm "benchmark/rest/storage/gorm"
	"context"
	"time"

	"github.com/lib/pq"
	"github.com/m4gshm/gollections/slice"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

//go:generate fieldr --debug
//go:fieldr -type Task
//go:fieldr enum-const -export -val "field.name" -name "{{(join struct.name \"Field\" field.name)}}"
//go:fieldr enum-const -export -val "low field.name" -name "{{(join  struct.name \"Column\" field.name)}}"

const TABLE_TASK = "task"

type Task struct {
	ID       string     `gorm:"primaryKey"`
	Text     string     ``
	Tags     []*TaskTag  `gorm:"foreignKey:TaskID"`
	Deadline *time.Time `gorm:"type:timestamp"`
}

var _ storage.IDAware[string] = (*Task)(nil)
var _ sgorm.IDColNameAware = (*Task)(nil)
var _ Tabler = (*Task)(nil)
var _ sgorm.SelfSave = (*Task)(nil)
var _ sgorm.SelfDelete[string] = (*Task)(nil)

// GetId implements storage.IDAware
func (t *Task) GetId() string {
	return t.ID
}

// SetId implements storage.IDAware
func (t *Task) SetId(id string) {
	t.ID = id
}

type Tabler interface {
	TableName() string
}

// TableName 'task'
func (*Task) TableName() string {
	return TABLE_TASK
}

// IDColName implements gorm.IDColNameAware
func (*Task) IDColName() string {
	return TaskColumnID
}

// Store implements gorm.ActiveStore
func (t *Task) Save(ctx context.Context, db *gorm.DB) error {
	return db.Transaction(func(tx *gorm.DB) error {
		if err := t.deleteTags(tx); err != nil {
			return err
		} else if err := tx.Session(&gorm.Session{FullSaveAssociations: true}).
			Clauses(clause.OnConflict{UpdateAll: true}).Create(t).Error; err != nil {
			return err
		} else {
			return nil
		}
	})
}

// Delete implements gorm.SelfDelete
func (t *Task) DeleteByID(ctx context.Context, db *gorm.DB, id string) (bool, error) {
	found := true
	err := db.Transaction(func(tx *gorm.DB) error {
		dt := &Task{ID: id}
		if err := dt.deleteTags(tx); err != nil {
			return err
		} else {
			del := tx.Delete(dt)
			if err := del.Error; err != nil {
				return err
			} else {
				found = del.RowsAffected > 0
				return nil
			}
		}
	})
	return found, err
}

func (t *Task) deleteTags(tx *gorm.DB) error {
	if t == nil {
		return nil
	}

	tags := slice.StringsBehaveAs[pq.StringArray](slice.Convert(t.Tags, func(tag *TaskTag) string { return tag.Tag }))

	return tx.Where(TaskTagColumnTaskID+"=? and not "+TaskTagColumnTag+"=any(?)", t.ID, tags).Delete(&TaskTag{}).Error
}
