package task

import "benchmark/rest/model"
import "github.com/m4gshm/gollections/slice"

func ConvertToGorm(task *model.Task) *Task {
	return &Task{
		ID:       task.ID,
		Text:     task.Text,
		Deadline: task.Deadline,
		Tags:     convertTagsDtoToGorm(task.Tags),
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

func convertTagsDtoToGorm(tags []string) []Tag {
	return slice.Map(tags, func(tag string) Tag { return Tag{Tag: tag} })
}

func convertTagsGormToDto(tags []Tag) []string {
	return slice.Map(tags, func(tag Tag) string { return tag.Tag })
}
