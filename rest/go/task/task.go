package task

import "time"

type Task struct {
	Id       string    `json:"id"`
	Text     string    `json:"text"`
	Tags     []string  `json:"tags"`
	Deadline time.Time `json:"deadline" format:"date-time"`
}
