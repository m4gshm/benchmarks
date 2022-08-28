package task

import (
	"benchmark/rest/storage"
	"time"
)

const TABLE_TASK = "task"

type Task struct {
	Id   string `json:"id,omitempty" gorm:"primaryKey"`
	Text string `json:"text,omitempty"`
	// Tags     []string   `json:"tags,omitempty"`
	Deadline *time.Time `json:"deadline,omitempty" format:"date-time"`
}

var _ storage.IDAware[string] = (*Task)(nil)
var _ Tabler = (*Task)(nil)

// GetId implements storage.IDAware
func (t *Task) GetId() string {
	return t.Id
}

// SetId implements storage.IDAware
func (t *Task) SetId(id string) {
	t.Id = id
}

type Tabler interface {
	TableName() string
}

// TableName 'taskentity'
func (*Task) TableName() string {
	return "taskentity"
}
