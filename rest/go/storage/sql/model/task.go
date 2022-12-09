package model

import (
	"benchmark/rest/model"
	"strings"

	"github.com/m4gshm/gollections/slice"
)

//go:generate fieldr -type Task -in ../../../model/task.go
//go:fieldr enum-const -export -val "field.name" -name "{{(join struct.name \"Field\" field.name)}}"
//go:fieldr enum-const -export -type "TaskColumn" -val "low field.name" -name "{{(join  struct.name \"Column\" field.name)}}" -list "TaskColumns" -ref-access --exclude Tags

func SqlTaskColumns() string {
	return strings.Join(slice.BehaveAsStrings(TaskColumns()), ",")
}

func TaskIdColumn() string {
	return string(TaskColumnID)
}

func SqlTaskColumnFiledReferences(t *model.Task) []any {
	return slice.Map(TaskColumns(), func(c TaskColumn) any { return Ref(t, c) })
}
