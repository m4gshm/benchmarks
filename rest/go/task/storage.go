package task

import (
	"context"
	"runtime/trace"
	"sync"

	"github.com/google/uuid"
)

type ID = string

type Storage interface {
	Store(context.Context, *Task) (ID, error)
	Delete(context.Context, ID) error
	Get(context.Context, ID) (*Task, error)
	List(context.Context) ([]*Task, error)
}

func NewMemoryStorage() *MemoryStorage {
	return &MemoryStorage{tasks: map[string]*Task{}}
}

type MemoryStorage struct {
	tasks  map[string]*Task
	locker sync.RWMutex
}

var _ Storage = (*MemoryStorage)(nil)

var mem_storage_pref = "MemoryStorage."

// Delete implements Storage
func (s *MemoryStorage) Delete(ctx context.Context, id string) error {
	_, t := trace.NewTask(ctx, mem_storage_pref+"Delete")
	defer t.End()
	s.locker.Lock()
	delete(s.tasks, id)
	s.locker.Unlock()
	return nil
}

// Get implements Storage
func (s *MemoryStorage) Get(ctx context.Context, id string) (*Task, error) {
	_, t := trace.NewTask(ctx, mem_storage_pref+"Get")
	defer t.End()
	s.locker.RLock()
	task := s.tasks[id]
	s.locker.RUnlock()
	return task, nil
}

// List implements Storage
func (s *MemoryStorage) List(ctx context.Context) ([]*Task, error) {
	_, t := trace.NewTask(ctx, mem_storage_pref+"List")
	defer t.End()
	s.locker.RLock()
	tasks := make([]*Task, 0, len(s.tasks))
	for _, task := range s.tasks {
		tasks = append(tasks, task)
	}
	s.locker.RUnlock()
	return tasks, nil
}

// Store implements Storage
func (s *MemoryStorage) Store(ctx context.Context, task *Task) (string, error) {
	_, t := trace.NewTask(ctx, mem_storage_pref+"Store")
	defer t.End()
	id := task.Id
	if len(id) == 0 {
		id = uuid.NewString()
		task.Id = id
	}
	s.locker.Lock()
	if s.tasks[id] == nil {
		s.tasks[id] = task
	}
	s.locker.Unlock()
	return id, nil
}
