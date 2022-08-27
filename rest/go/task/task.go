package task

import (
	"benchmark/rest/storage"
	"time"
)

type Task struct {
	Id       string     `json:"id,omitempty"`
	Text     string     `json:"text,omitempty"`
	// Tags     []string   `json:"tags,omitempty"`
	Deadline *time.Time `json:"deadline,omitempty" format:"date-time"`
}

var _ storage.IDAware[string] = (*Task)(nil)

// GetId implements storage.IDAware
func (t *Task) GetId() string {
	return t.Id
}

// SetId implements storage.IDAware
func (t *Task) SetId(id string) {
	t.Id = id
}
