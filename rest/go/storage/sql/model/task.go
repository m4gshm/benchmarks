package model

import (
	"benchmark/rest/model"
	"fmt"
	"strings"

	"github.com/m4gshm/gollections/slice"
)

//go:generate fieldr -type Task -in ../../../model/task.go
//go:fieldr enum-const -export -val "field.name" -name "{{(join struct.name \"Field\" field.name)}}"
//go:fieldr enum-const -export -val "field.type.Type" -name "{{(join struct.name \"FieldType\" field.name)}}" -list TaskColumnTypes -exclude Tags
//go:fieldr enum-const -export -type "TaskColumn" -val "low field.name" -name "{{(join  struct.name \"Column\" field.name)}}" -list "TaskColumns" -list . -ref-access . -exclude Tags -check-unique-val

func SqlTaskColumns() string {
	return strings.Join(slice.BehaveAsStrings(TaskColumns()), ",")
}

func TaskIdColumn() string {
	return string(TaskColumnID)
}

func SqlTaskColumnFieldPlaceholders() string {
	return strings.Join(slice.MapIndex(TaskColumns(), func(i int, _ TaskColumn) string { return fmt.Sprintf("$%d", i+1) }), ",")
}

func SqlTaskColumnUpdateExpr() string {
	return strings.Join(
		slice.MapCheckIndex(TaskColumns(),
			func(i int, col TaskColumn) (string, bool) {
				return string(col) + fmt.Sprintf("=$%d", i+1), col != TaskColumnID
			},
		),
		",")
}

func SqlTaskColumnFieldReferences(t *model.Task) []any {
	return slice.Map(TaskColumns(), func(c TaskColumn) any { return Ref(t, c) })
}
