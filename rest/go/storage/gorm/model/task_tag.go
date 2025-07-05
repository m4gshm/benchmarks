package model

//go:generate fieldr -out . --debug
//go:fieldr -type TaskTag
//go:fieldr new-full -return-value
//go:fieldr get-set

//go:fieldr fields-to-consts -export -val "field.name" -name "join(struct.name,\"Field\",field.name)"
//go:fieldr fields-to-consts -export -val "low(snake(field.name))" -name "join(struct.name,\"Column\",field.name)"

type TaskTag struct {
	TaskID string `gorm:"primaryKey"`
	Tag    string `gorm:"primaryKey"`
}

// TableName implements Tabler
func (TaskTag) TableName() string {
	return "task_tag"
}

var _ Tabler = (*TaskTag)(nil)
