package model

import (
	"time"

	"benchmark/rest/storage"
)

type Task struct {
	ID       string     `json:"id,omitempty"`
	Text     string     `json:"text,omitempty"`
	Tags     []string   `json:"tags,omitempty"`
	Deadline *time.Time `json:"deadline,omitempty" format:"date-time"`
}

// GetId implements storage.IDAware
func (t *Task) GetId() string {
	return t.ID
}

// SetId implements storage.IDAware
func (t *Task) SetId(id string) {
	t.ID = id
}

var _ storage.IDAware[string] = (*Task)(nil)
