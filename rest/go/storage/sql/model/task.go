package model

import (
	"benchmark/rest/model"

	"github.com/m4gshm/gollections/slice"
)

//go:generate fieldr -type Task -in ../../../model/task.go
//go:fieldr enum-const -export -val "field.name" -name "{{(join struct.name \"Field\" field.name)}}"
//go:fieldr enum-const -export -type "TaskColumn" -val "low field.name" -name "{{(join  struct.name \"Column\" field.name)}}" -list "TaskColumns" -ref-access

func GetAllColumns(*model.Task) []string {
	return slice.BehaveAsStrings(TaskColumns())
}

func GetIdColumn(*model.Task) string {
	return string(TaskColumnID)
}

func GetAllColumnFiledRefs(t *model.Task) []any {
	return slice.Map(TaskColumns(), func(c TaskColumn) any { return Ref(t, c) })
}
