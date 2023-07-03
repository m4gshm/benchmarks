package decorator

import (
	"benchmark/rest/storage"
	"context"

	"github.com/m4gshm/gollections/c"
	"github.com/m4gshm/gollections/k"
	"github.com/m4gshm/gollections/slice"
)

func Wrap[IN any, OUT any, ID any](storage storage.API[IN, ID], toInternalConverter func(OUT) IN, toExternalConverter func(IN) OUT) storage.API[OUT, ID] {
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
	return tryConvert(catchOne(r.storage.List(ctx)), func(ins []IN) []OUT { return slice.Convert(ins, r.toOut) })
}

// Store implements storage.API
func (r *RepoDeco[IN, OUT, ID]) Store(ctx context.Context, value OUT) (OUT, error) {
	return tryConvert(catchOne(r.storage.Store(ctx, r.toIn(value))),  r.toOut)
}

var _ storage.API[any, any] = (*RepoDeco[any, any, any])(nil)

func catchOne[I any](in I, err error) c.KV[I, error] {
	return k.V(in, err)
}

func tryConvert[I, O any](state c.KV[I, error], converter func(I) O) (out O, err error) {
	err = state.V
	if err == nil {
		out = converter(state.K)
	}
	return out, err
}