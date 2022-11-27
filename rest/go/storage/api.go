package storage

import "context"

type API[T any, ID any] interface {
	Store(context.Context, T) (T, error)
	Delete(context.Context, ID) (bool, error)
	Get(context.Context, ID) (T, bool, error)
	List(context.Context) ([]T, error)
}

type IDAware[ID any] interface {
	GetId() ID
	SetId(id ID)
}
