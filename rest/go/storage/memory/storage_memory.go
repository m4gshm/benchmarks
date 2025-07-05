package memory

import (
	"benchmark/rest/storage"
	"context"
	"runtime/trace"
	"sync"

	"github.com/m4gshm/gollections/map_"
)

type ID = string

func NewStorage[T storage.IDAware[ID], ID comparable]() *Storage[T, ID] {
	return &Storage[T, ID]{entities: map[ID]T{}}
}

type Storage[T storage.IDAware[ID], ID comparable] struct {
	entities map[ID]T
	locker   sync.RWMutex
}

var _ storage.API[storage.IDAware[string], string] = (*Storage[storage.IDAware[string], string])(nil)

var storage_pref = "MemoryStorage."

// Delete implements Storage
func (s *Storage[T, ID]) Delete(ctx context.Context, id ID) (ok bool, err error) {
	_, t := trace.NewTask(ctx, storage_pref+"Delete")
	defer t.End()
	s.locker.Lock()
	if _, ok = s.entities[id]; ok {
		delete(s.entities, id)
	}
	s.locker.Unlock()
	return ok, nil
}

// Get implements Storage
func (s *Storage[T, ID]) Get(ctx context.Context, id ID) (T, bool, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Get")
	defer t.End()
	s.locker.RLock()
	task, found := s.entities[id]
	s.locker.RUnlock()
	return task, found, nil
}

// List implements Storage
func (s *Storage[T, ID]) List(ctx context.Context) ([]T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"List")
	defer t.End()
	s.locker.RLock()
	tasks := map_.Values(s.entities)
	s.locker.RUnlock()
	return tasks, nil
}

// Store implements Storage
func (s *Storage[T, ID]) Store(ctx context.Context, entity T) (T, error) {
	_, t := trace.NewTask(ctx, storage_pref+"Store")
	defer t.End()
	s.locker.Lock()
	id := entity.GetID()
	s.entities[id] = entity
	s.locker.Unlock()
	return entity, nil
}

// Update implements Storage
func (s *Storage[T, ID]) Update(ctx context.Context, entity T) (T, error) {
	return s.Store(ctx, entity)
}
