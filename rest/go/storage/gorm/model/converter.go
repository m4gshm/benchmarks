package model

import (
	"benchmark/rest/model"

	"github.com/m4gshm/gollections/slice"
)

func ConvertToGorm(task *model.Task) *Task {
	return NewTask(task.ID, task.Text, ConvertTagsDtoToGorm(task.Tags, task.ID), task.Deadline)
}

func ConvertToDto(task *Task) *model.Task {
	return model.NewTask(task.ID, task.Text, ConvertTagsGormToDto(task.Tags), task.Deadline)
}

func ConvertTagsDtoToGorm(tags []string, taskID string) []*TaskTag {
	return slice.Convert(tags, func(tag string) *TaskTag { return NewTaskTag(tag, taskID) })
}

func ConvertTagsGormToDto(tags []*TaskTag) []string {
	return slice.Convert(tags, (*TaskTag).GetTag)
}
