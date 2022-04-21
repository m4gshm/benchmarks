package task

import "time"

type Task struct {
	Id       string    `json:"id,omitempty"`
	Text     string    `json:"text,omitempty"`
	Tags     []string  `json:"tags,omitempty"`
	Deadline time.Time `json:"deadline,omitempty" format:"date-time"`
}
