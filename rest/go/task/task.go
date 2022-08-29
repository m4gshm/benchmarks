package task

import (
	"benchmark/rest/storage"
	"benchmark/rest/storage/gorm"
	"time"
)

const TABLE_TASK = "task"
const COLUMNT_ID = "id"

type Task struct {
	ID   string `json:"id,omitempty" gorm:"primaryKey"`
	Text string `json:"text,omitempty"`
	// Tags     []string   `json:"tags,omitempty"`
	Deadline *time.Time `json:"deadline,omitempty" format:"date-time"`
}

var _ storage.IDAware[string] = (*Task)(nil)
var _ gorm.IDColNameAware = (*Task)(nil)
var _ Tabler = (*Task)(nil)

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

// TableName 'taskentity'
func (*Task) TableName() string {
	return "task"
}


// IDColName implements gorm.IDColNameAware
func (*Task) IDColName() string {
	return COLUMNT_ID
}
