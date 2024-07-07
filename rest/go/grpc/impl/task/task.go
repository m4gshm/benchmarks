package task

import (
	"context"
	"time"

	"github.com/m4gshm/gollections/convert/ptr"
	"github.com/m4gshm/gollections/expr/use"
	"github.com/m4gshm/gollections/slice"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	timestamppb "google.golang.org/protobuf/types/known/timestamppb"

	"benchmark/rest/grpc/gen/go/task"
	"benchmark/rest/model"
	"benchmark/rest/storage"
)

type TaskServiceServerIml struct {
	Storage storage.API[*model.Task, string]
}

func (t *TaskServiceServerIml) Get(ctx context.Context, r *task.TaskServiceIDRequest) (*task.TaskServiceTask, error) {
	id := r.Id
	exists, ok, err := t.Storage.Get(ctx, id)
	if err != nil {
		return nil, err
	} else if !ok {
		return nil, status.Error(codes.NotFound, "Task not found")
	}
	return taskDbToGrpc(exists), nil
}

// GetAll implements task.TaskServiceServer.
func (t *TaskServiceServerIml) GetAll(ctx context.Context, _ *task.TaskServiceGetAllRequest) (*task.TaskServiceTasks, error) {
	exists, err := t.Storage.List(ctx)
	if err != nil {
		return nil, err
	}
	return &task.TaskServiceTasks{Tasks: slice.Convert(exists, taskDbToGrpc)}, nil
}

// Store implements tsk.TaskServiceServer.
func (t *TaskServiceServerIml) Store(ctx context.Context, r *task.TaskServiceTask) (*task.TaskServiceTask, error) {
	s := taskGrpcToDb(r)
	exists, err := t.Storage.Store(ctx, s)
	if err != nil {
		return nil, err
	}
	return taskDbToGrpc(exists), nil
}

func (t *TaskServiceServerIml) Delete(ctx context.Context, r *task.TaskServiceIDRequest) (*task.TaskServiceDeleteResponse, error) {
	id := r.Id
	ok, err := t.Storage.Delete(ctx, id)
	if err != nil {
		return nil, err
	} else if !ok {
		return nil, status.Error(codes.NotFound, "Task not found")
	}
	return nil, nil
}

func taskDbToGrpc(t *model.Task) *task.TaskServiceTask {
	var dl *timestamppb.Timestamp
	if d := t.Deadline; d != nil {
		dl = timestamppb.New(*d)
	}
	return &task.TaskServiceTask{
		Id:       t.ID,
		Text:     t.Text,
		Tags:     t.Tags,
		Deadline: dl,
	}
}

func taskGrpcToDb(t *task.TaskServiceTask) *model.Task {
	return &model.Task{
		ID:       t.Id,
		Text:     t.Text,
		Tags:     t.Tags,
		Deadline: use.IfGet(t.Deadline != nil, func() *time.Time { return ptr.Of(t.Deadline.AsTime()) }).Else(nil),
	}
}

func convertNoNil[From, To any](pointer *From, converter func(*From) To) (t To, ok bool) {
	if pointer != nil {
		return converter(pointer), true
	}
	return t, false
}

var _ task.TaskServiceServer = (*TaskServiceServerIml)(nil)
