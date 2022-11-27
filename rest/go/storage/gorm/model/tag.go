package task

// import "gorm.io/gorm"

type Tag struct {
	// gorm.Model
	// ID  uint   `gorm:"primaryKey"`
	Tag string `gorm:"primaryKey"`
}

// TableName implements Tabler
func (*Tag) TableName() string {
	return "tag"
}

var _ Tabler = (*Tag)(nil)
