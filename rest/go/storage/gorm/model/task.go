package model

import (
	"benchmark/rest/storage"
	sgorm "benchmark/rest/storage/gorm"
	"time"

	"github.com/lib/pq"
	"github.com/m4gshm/gollections/slice"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

//go:generate fieldr --debug
//go:fieldr -type Task
//go:fieldr new-opt -name NewTaskWith -suffix . -return-value
//go:fieldr new-full -return-value
//go:fieldr get-set
//go:fieldr fields-to-consts -export -val "field.name" -name "join(struct.name,\"Field\",field.name)"
//go:fieldr fields-to-consts -export -val "low(field.name)" -name "join(struct.name,\"Column\",field.name)"

const TABLE_TASK = "task"

type Task struct {
	ID       string    `gorm:"primaryKey"`
	Text     string    ``
	Tags     []TaskTag `gorm:"foreignKey:TaskID"`
	Deadline time.Time `gorm:"type:timestamp"`
}

var _ storage.IDAware[string] = (*Task)(nil)
var _ sgorm.IDColNameAware = (*Task)(nil)
var _ Tabler = (*Task)(nil)

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
func (t *Task) Save(db *gorm.DB) error {
	return db.Transaction(func(tx *gorm.DB) error {
		if err := t.deleteUnusedTags(tx); err != nil {
			return err
		} else if err := tx.Session(&gorm.Session{FullSaveAssociations: true}).
			Clauses(clause.OnConflict{UpdateAll: true}).Create(t).Error; err != nil {
			return err
		} else {
			return nil
		}
	})
}

// Delete deletes task by id
func DeleteByID(db *gorm.DB, id string) (bool, error) {
	found := true
	err := db.Transaction(func(tx *gorm.DB) error {
		dt := NewTaskWith(TaskID(id))
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

func (t *Task) deleteTags(tx *gorm.DB) (err error) {
	if t != nil {
		err = tx.Where(TaskTagColumnTaskID+"=?", t.ID).Delete(&TaskTag{}).Error
	}
	return
}

func (t *Task) deleteUnusedTags(tx *gorm.DB) (err error) {
	if t != nil {
		tags := slice.StringsBehaveAs[pq.StringArray](slice.Convert(t.Tags, func(t TaskTag) string { return t.GetTaskID() }))
		if len(tags) > 0 {
			err = tx.Where(TaskTagColumnTaskID+"=? and not "+TaskTagColumnTag+"=any(?)", t.ID, tags).Delete(&TaskTag{}).Error
		}
	}
	return
}
