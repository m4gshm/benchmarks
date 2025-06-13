package model

import (
	"benchmark/rest/storage"
	"time"
)

//go:generate fieldr -type Task --debug
//go:fieldr new-full
//go:fieldr get-set

type Task struct {
	ID       string     `json:"id,omitempty"`
	Text     string     `json:"text,omitempty"`
	Tags     []string   `json:"tags,omitempty"`
	Deadline *time.Time `json:"deadline,omitempty" format:"date-time"`
}

var _ storage.IDAware[string] = (*Task)(nil)
