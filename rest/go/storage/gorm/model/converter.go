package model

import (
	"benchmark/rest/model"

	"github.com/m4gshm/gollections/slice"
)

func ConvertToGorm(task *model.Task) *Task {
	return &Task{
		ID:       task.ID,
		Text:     task.Text,
		Deadline: task.Deadline,
		Tags:     ConvertTagsDtoToGorm(task.Tags, task.ID),
	}
}

func ConvertToDto(task *Task) *model.Task {
	return &model.Task{
		ID:       task.ID,
		Text:     task.Text,
		Deadline: task.Deadline,
		Tags:     ConvertTagsGormToDto(task.Tags),
	}
}

func ConvertTagsDtoToGorm(tags []string, taskID string) []*TaskTag {
	return slice.Convert(tags, func(tag string) *TaskTag { return &TaskTag{Tag: tag, TaskID: taskID} })
}

func ConvertTagsGormToDto(tags []*TaskTag) []string {
	return slice.Convert(tags, func(tag *TaskTag) string { return tag.Tag })
}
