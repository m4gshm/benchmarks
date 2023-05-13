package model

//go:generate fieldr --debug
//go:fieldr -type TaskTag
//go:fieldr enum-const -export -val "field.name" -name "{{(join struct.name \"Field\" field.name)}}"
//go:fieldr enum-const -export -val "low (snake field.name)" -name "{{(join  struct.name \"Column\" field.name)}}"
//go:fieldr get-set 

type TaskTag struct {
	TaskID string `gorm:"primaryKey"`
	Tag    string `gorm:"primaryKey"`
}

// TableName implements Tabler
func (*TaskTag) TableName() string {
	return "task_tag"
}

var _ Tabler = (*TaskTag)(nil)
