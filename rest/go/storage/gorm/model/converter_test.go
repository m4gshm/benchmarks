package model

import (
	"benchmark/rest/model"
	"testing"
	"time"

	"github.com/m4gshm/gollections/slice"
	"github.com/stretchr/testify/assert"
)

func Test_ConvertToGorm(t *testing.T) {
	deadline := time.Now()
	expected := NewTask("id", "text", slice.Of(NewTaskTag("id", "tag1"), NewTaskTag("id", "tag2")), deadline)

	src := model.NewTask("id", "text", slice.Of("tag1", "tag2"), deadline)
	result := ConvertToGorm(&src)

	assert.Equal(t, expected, *result)
}

func Test_ConvertToDto(t *testing.T) {
	deadline := time.Now()
	expected := model.NewTask("id", "text", slice.Of("tag1", "tag2"), deadline)
	
	src := NewTask("id", "text", slice.Of(NewTaskTag("id", "tag1"), NewTaskTag("id", "tag2")), deadline)
	result := ConvertToDto(&src)

	assert.Equal(t, expected, *result)
}