package decorator

import (
	"benchmark/rest/storage"
	"context"

	"github.com/m4gshm/gollections/slice"
)

func Warp[IN any, OUT any, ID any](storage storage.API[IN, ID], toInternalConverter func(OUT) IN, toExternalConverter func(IN) OUT) storage.API[OUT, ID] {
	return &RepoDeco[IN, OUT, ID]{storage: storage, toIn: toInternalConverter, toOut: toExternalConverter}
}

type RepoDeco[IN any, OUT any, ID any] struct {
	storage storage.API[IN, ID]
	toOut   func(IN) OUT
	toIn    func(OUT) IN
}

// Delete implements storage.API
func (r *RepoDeco[IN, OUT, ID]) Delete(ctx context.Context, id ID) (bool, error) {
	return r.storage.Delete(ctx, id)
}

// Get implements storage.API
func (r *RepoDeco[IN, OUT, ID]) Get(ctx context.Context, id ID) (OUT, bool, error) {
	in, ok, err := r.storage.Get(ctx, id)
	if err != nil || !ok {
		var no OUT
		return no, ok, err
	}
	return r.toOut(in), ok, nil
}

// List implements storage.API
func (r *RepoDeco[IN, OUT, ID]) List(ctx context.Context) ([]OUT, error) {
	ins, err := r.storage.List(ctx)
	if err != nil {
		return nil, err
	}
	return slice.Convert(ins, r.toOut), nil
}

// Store implements storage.API
func (r *RepoDeco[IN, OUT, ID]) Store(ctx context.Context, value OUT) (OUT, error) {
	stored, err := r.storage.Store(ctx, r.toIn(value))
	if err != nil {
		var no OUT
		return no, err
	}
	return r.toOut(stored), nil
}

var _ storage.API[any, any] = (*RepoDeco[any, any, any])(nil)
