package task

import (
	"benchmark/rest/storage"
	sgorm "benchmark/rest/storage/gorm"
	"context"
	"gorm.io/gorm"
	"time"
	// "gorm.io/gorm"
)

//go:generate fieldr -type Task
//go:fieldr  enum-const -export -val "field.name" -name "{{(join struct.name \"Field\" field.name)}}"
//go:fieldr enum-const -export -val "low field.name" -name "{{(join  struct.name \"Column\" field.name)}}"

const TABLE_TASK = "task"

type Task struct {
	ID       string     `gorm:"primaryKey"`
	Text     string     ``
	Tags     []Tag      `gorm:"many2many:task_tag"`
	Deadline *time.Time `gorm:"type:timestamp"`
}

var _ storage.IDAware[string] = (*Task)(nil)
var _ sgorm.IDColNameAware = (*Task)(nil)
var _ Tabler = (*Task)(nil)
var _ sgorm.SelfSave = (*Task)(nil)

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
		if tags := t.Tags; len(tags) > 0 {
			if err := db.Model(t).Association(TaskFieldTags).Clear(); err != nil {
				return err
			}
			t.Tags = tags
		}
		return sgorm.DefaultSave(tx, t).Error
	})
}
