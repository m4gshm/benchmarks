package memory

import (
	"benchmark/rest/storage"
	"context"
	"runtime/trace"
	"sync"
)

type ID = string

func NewMemoryStorage[T storage.IDAware[ID], ID comparable]() *MemoryStorage[T, ID] {
	return &MemoryStorage[T, ID]{entities: map[ID]T{}}
}

type MemoryStorage[T storage.IDAware[ID], ID comparable] struct {
	entities map[ID]T
	locker   sync.RWMutex
}

var _ storage.API[storage.IDAware[string], string] = (*MemoryStorage[storage.IDAware[string], string])(nil)

var mem_storage_pref = "MemoryStorage."

// Delete implements Storage
func (s *MemoryStorage[T, ID]) Delete(ctx context.Context, id ID) (bool, error) {
	_, t := trace.NewTask(ctx, mem_storage_pref+"Delete")
	defer t.End()
	s.locker.Lock()
	_, ok := s.entities[id]
	if ok {
		delete(s.entities, id)
	}
	s.locker.Unlock()
	return ok, nil
}

// Get implements Storage
func (s *MemoryStorage[T, ID]) Get(ctx context.Context, id ID) (T, bool, error) {
	_, t := trace.NewTask(ctx, mem_storage_pref+"Get")
	defer t.End()
	s.locker.RLock()
	task, found := s.entities[id]
	s.locker.RUnlock()
	return task, found, nil
}

// List implements Storage
func (s *MemoryStorage[T, ID]) List(ctx context.Context) ([]T, error) {
	_, t := trace.NewTask(ctx, mem_storage_pref+"List")
	defer t.End()
	s.locker.RLock()
	tasks := make([]T, 0, len(s.entities))
	for _, task := range s.entities {
		tasks = append(tasks, task)
	}
	s.locker.RUnlock()
	return tasks, nil
}

// Store implements Storage
func (s *MemoryStorage[T, ID]) Store(ctx context.Context, entity T) (T, error) {
	_, t := trace.NewTask(ctx, mem_storage_pref+"Store")
	defer t.End()
	s.locker.Lock()
	id := entity.GetId()
	s.entities[id] = entity
	s.locker.Unlock()
	return entity, nil
}

// Update implements Storage
func (s *MemoryStorage[T, ID]) Update(ctx context.Context, entity T) (T, error) {
	return s.Store(ctx, entity)
}
