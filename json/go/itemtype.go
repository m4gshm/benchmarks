package map_benchmark

import (
	"errors"
	"fmt"
	"reflect"
)

type ItemType int

const (
	Basic ItemType = iota
	Extended
)

func (i *ItemType) UnmarshalText(text []byte) error {
	str := string(text)
	switch str {
	case "basic":
		*i = Basic
	case "extended":
		*i = Extended
	default:
		return errors.New(fmt.Sprintf("uexpected value %v, enum %v", text, reflect.TypeOf(i)))
	}
	return nil
}

func (i *ItemType) MarshalText() (text []byte, err error) {
	return []byte(i.String()), nil
}

//go:generate stringer -type=ItemType
