package task

import (
	"context"
	"encoding/json"
	"net/http"
	"runtime/trace"

	"github.com/go-chi/chi/v5"
)

func NewHandler(storage Storage) *Handler {
	return &Handler{storage: storage}
}

type Handler struct {
	storage Storage
}

// CreateTask godoc
// @Summary      Create task
// @Description  create task
// @Tags         task
// @Accept       json
// @Produce      json
// @Param        task	body      Task  	true  "Create task"
// @Success      200	{string}  string	"ok"
// @Failure      400	{string}  string    "error"
// @Failure      404	{string}  string    "error"
// @Failure      500 	{string}  string    "error"
// @Router       /task	[post]
func (h Handler) CreateTask(writer http.ResponseWriter, request *http.Request) {
	ctx, t := trace.NewTask(request.Context(), "CreateTask")
	defer t.End()
	if task, err := decodeBody(ctx, writer, request); err == nil {
		if err := h.store(ctx, task, writer); err == nil {
			successResponse(ctx, writer)
		}
	}
}

// UpdateTask godoc
// @Summary      Update task
// @Description  update task
// @Tags         task
// @Accept       json
// @Produce      json
// @Success      200	{array}   Task
// @Failure      400	{string}  string    "error"
// @Failure      404	{string}  string    "error"
// @Failure      500 	{string}  string    "error"
// @Router       /task/{id}	[put]
func (h Handler) UpdateTask(writer http.ResponseWriter, request *http.Request) {
	ctx, t := trace.NewTask(request.Context(), "UpdateTask")
	defer t.End()
	if task, err := decodeBody(ctx, writer, request); err == nil {
		if id := getId(request); len(id) > 0 {
			task.Id = id
		}
		if err := h.store(ctx, task, writer); err == nil {
			successResponse(ctx, writer)
		}
	}
}

// ListTasks godoc
// @Summary      List tasks
// @Description  list all tasks
// @Tags         task
// @Accept       json
// @Produce      json
// @Success      200	{array}   Task
// @Failure      400	{string}  string    "error"
// @Failure      404	{string}  string    "error"
// @Failure      500 	{string}  string    "error"
// @Router       /task	[get]
func (h Handler) ListTasks(writer http.ResponseWriter, request *http.Request) {
	ctx, t := trace.NewTask(request.Context(), "ListTasks")
	defer t.End()
	if tasks, err := h.storage.List(ctx); err != nil {
		http.Error(writer, "storage: "+err.Error(), http.StatusInternalServerError)
	} else {
		writeJsonEntityResponse(ctx, writer, tasks)
	}
}

// GetTask godoc
// @Summary      Show a task
// @Description  get task by ID
// @Tags         task
// @Accept       json
// @Produce      json
// @Param        id   		path      string  true  "Task ID"
// @Success      200  		{object}  Task
// @Failure      404		{string}  string    "error"
// @Router       /task/{id} [get]
func (h Handler) GetTask(writer http.ResponseWriter, request *http.Request) {
	ctx, t := trace.NewTask(request.Context(), "GetTask")
	defer t.End()
	if task, err := h.storage.Get(ctx, getId(request)); err != nil {
		http.Error(writer, "storage: "+err.Error(), http.StatusInternalServerError)
	} else if task != nil {
		writeJsonEntityResponse(ctx, writer, task)
	} else {
		writer.WriteHeader(http.StatusNotFound)
	}
}

// DeleteTask godoc
// @Summary      Delete a task
// @Description  delete task by ID
// @Tags         task
// @Accept       json
// @Produce      json
// @Param        id   		path      string  	true  "Task ID"
// @Success      200		{string}  string	"ok"
// @Failure      404		{string}  string    "error"
// @Router       /task/{id} [delete]
func (h Handler) DeleteTask(writer http.ResponseWriter, request *http.Request) {
	ctx, t := trace.NewTask(request.Context(), "DeleteTask")
	defer t.End()
	if err := h.storage.Delete(ctx, getId(request)); err != nil {
		http.Error(writer, "storage: "+err.Error(), http.StatusInternalServerError)
	} else {
		successResponse(ctx, writer)
	}

}

func getId(request *http.Request) string {
	id := chi.URLParam(request, "id")
	return id
}

func writeJsonEntityResponse(ctx context.Context, writer http.ResponseWriter, payload interface{}) {
	defer trace.StartRegion(ctx, "writeResponse").End()
	if js, err := json.Marshal(payload); err != nil {
		http.Error(writer, "json-encode: "+err.Error(), http.StatusInternalServerError)
	} else {
		writeJsonResponse(writer, js)
	}
}

func writeJsonResponse(writer http.ResponseWriter, payload []byte) {
	writer.Header().Set("Content-Type", "application/json")
	writer.Write(payload)
}

type Status struct {
	Success bool `json:"success"`
}

func successResponse(ctx context.Context, writer http.ResponseWriter) {
	writeJsonEntityResponse(ctx, writer, Status{Success: true})
}

func decodeBody(ctx context.Context, writer http.ResponseWriter, request *http.Request) (*Task, error) {
	defer trace.StartRegion(ctx, "decodeBody").End()
	var task Task
	if err := json.NewDecoder(request.Body).Decode(&task); err != nil {
		http.Error(writer, "json-decode: "+err.Error(), http.StatusBadRequest)
		return nil, err
	}
	return &task, nil
}

func (h Handler) store(ctx context.Context, task *Task, writer http.ResponseWriter) error {
	defer trace.StartRegion(ctx, "store").End()
	trace.Log(ctx, "taskId", task.Id)
	if _, err := h.storage.Store(ctx, task); err != nil {
		http.Error(writer, "storage: "+err.Error(), http.StatusInternalServerError)
		return err
	}
	return nil
}
