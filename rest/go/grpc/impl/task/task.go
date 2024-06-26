package task

import (
	"context"

	"github.com/m4gshm/gollections/slice"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	timestamppb "google.golang.org/protobuf/types/known/timestamppb"

	"benchmark/rest/grpc/gen/go/task"
	"benchmark/rest/model"
	"benchmark/rest/storage"
)

type TaskServiceServerIml struct {
	task.UnimplementedTaskServiceServer
	Storage storage.API[*model.Task, string]
}

func (t *TaskServiceServerIml) Get(ctx context.Context, r *task.TaskServiceGetRequest) (*task.TaskServiceGetResponse, error) {
	id := r.Id
	exists, ok, err := t.Storage.Get(ctx, id)
	if err != nil {
		return nil, err
	} else if !ok {
		return nil, status.Error(codes.NotFound, "Task not found")
	}

	var dl *timestamppb.Timestamp
	if d := exists.Deadline; d != nil {
		dl = timestamppb.New(*d)
	}

	return &task.TaskServiceGetResponse{
		Id:       exists.ID,
		Text:     exists.Text,
		Tags:     slice.Clone(exists.Tags),
		Deadline: dl,
	}, nil
}

var _ task.TaskServiceServer = (*TaskServiceServerIml)(nil)
