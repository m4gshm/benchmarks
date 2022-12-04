package task

import (
	"benchmark/rest/model"

	"github.com/m4gshm/gollections/slice"
)

func ConvertToGorm(task *model.Task) *Task {
	return &Task{
		ID:       task.ID,
		Text:     task.Text,
		Deadline: task.Deadline,
		Tags:     convertTagsDtoToGorm(task.Tags, task.ID),
	}
}

func ConvertToDto(task *Task) *model.Task {
	return &model.Task{
		ID:       task.ID,
		Text:     task.Text,
		Deadline: task.Deadline,
		Tags:     convertTagsGormToDto(task.Tags),
	}
}

func convertTagsDtoToGorm(tags []string, taskID string) []TaskTag {
	return slice.Map(tags, func(tag string) TaskTag { return TaskTag{Tag: tag, TaskID: taskID} })
}

func convertTagsGormToDto(tags []TaskTag) []string {
	return slice.Map(tags, func(tag TaskTag) string { return tag.Tag })
}
