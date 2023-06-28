package http

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"runtime/trace"

	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
	log "github.com/sirupsen/logrus"

	_ "benchmark/rest/model"
	"benchmark/rest/storage"
)

func StringID(request *http.Request) (string, bool, error) {
	id := chi.URLParam(request, "id")
	return id, len(id) > 0, nil
}

func UUIDGen() (string, error) {
	return uuid.NewString(), nil
}

type IdRetriever[ID any] func(request *http.Request) (ID, bool, error)

type IdGenerator[ID any] func() (ID, error)

var _ IdRetriever[string] = StringID
var _ IdGenerator[string] = UUIDGen

func NewHandler[T storage.IDAware[ID], ID comparable](storage storage.API[T, ID], idRetriever IdRetriever[ID], idGenerator IdGenerator[ID]) *Handler[T, ID] {
	return &Handler[T, ID]{storage: storage, idRetriever: idRetriever, idGenerator: idGenerator}
}

type Handler[T storage.IDAware[ID], ID comparable] struct {
	storage     storage.API[T, ID]
	idRetriever IdRetriever[ID]
	idGenerator IdGenerator[ID]
}

const handler_pref = "HttpHandler."

// CreateTask godoc
// @Summary      Create task
// @Description  create task
// @Tags         task
// @Accept       json
// @Produce      json
// @Param        task	body      model.Task  	true  "Create task"
// @Success      200	{string}  string	"ok"
// @Failure      400	{string}  string    "error"
// @Failure      404	{string}  string    "error"
// @Failure      500 	{string}  string    "error"
// @Router       /task	[post]
func (h Handler[T, ID]) CreateTask(writer http.ResponseWriter, request *http.Request) {
	ctx, t := trace.NewTask(request.Context(), handler_pref+"CreateTask")
	defer t.End()
	if entity, ok := decodeBody[T](ctx, writer, request); ok {
		var newId, noId ID
		if id := entity.GetId(); id == noId {
			if genId, err := h.idGenerator(); err != nil {
				http.Error(writer, "idgen: "+err.Error(), http.StatusInternalServerError)
			} else {
				entity.SetId(genId)
				newId = genId
			}
		}
		if err := h.store(ctx, "create", entity, writer); err == nil {
			writeJsonEntityResponse(ctx, writer, Status[ID]{Id: newId, Success: true})
		}
	}
}

// UpdateTask godoc
// @Summary      Update task
// @Description  update task
// @Tags         task
// @Accept       json
// @Produce      json
// @Success      200	{array}   model.Task
// @Failure      400	{string}  string    "error"
// @Failure      404	{string}  string    "error"
// @Failure      500 	{string}  string    "error"
// @Router       /task/{id}	[put]
func (h Handler[T, ID]) UpdateTask(writer http.ResponseWriter, request *http.Request) {
	ctx, t := trace.NewTask(request.Context(), handler_pref+"UpdateTask")
	defer t.End()
	if entity, ok := decodeBody[T](ctx, writer, request); ok {
		if id, ok, err := h.idRetriever(request); err != nil {
			internalErrOut(writer, "id", err)
		} else if ok {
			entity.SetId(id)
		}
		if err := h.store(ctx, "update", entity, writer); err == nil {
			successResponse(ctx, writer)
		}
	}
}

func (h Handler[T, ID]) store(ctx context.Context, name string, entity T, writer http.ResponseWriter) error {
	defer trace.StartRegion(ctx, name).End()
	trace.Log(ctx, "entityId", fmt.Sprint(entity.GetId()))
	if _, err := h.storage.Store(ctx, entity); err != nil {
		internalErrOut(writer, "storage-store", err)
		return err
	}
	return nil
}

// ListTasks godoc
// @Summary      List tasks
// @Description  list all tasks
// @Tags         task
// @Accept       json
// @Produce      json
// @Success      200	{array}   model.Task
// @Failure      400	{string}  string    "error"
// @Failure      404	{string}  string    "error"
// @Failure      500 	{string}  string    "error"
// @Router       /task	[get]
func (h Handler[T, ID]) ListTasks(writer http.ResponseWriter, request *http.Request) {
	ctx, t := trace.NewTask(request.Context(), handler_pref+"ListTasks")
	defer t.End()
	if tasks, err := h.storage.List(ctx); err != nil {
		internalErrOut(writer, "storage-list", err)
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
// @Success      200  		{object}  model.Task
// @Failure      404		{string}  string    "error"
// @Router       /task/{id} [get]
func (h Handler[T, ID]) GetTask(writer http.ResponseWriter, request *http.Request) {
	ctx, t := trace.NewTask(request.Context(), handler_pref+"GetTask")
	defer t.End()
	if id, ok, err := h.idRetriever(request); err != nil {
		internalErrOut(writer, "id", err)
	} else if !ok {
		badRequestOut(writer, "empty id", err)
	} else if task, found, err := h.storage.Get(ctx, id); err != nil {
		internalErrOut(writer, "storage-get", err)
	} else if found {
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
func (h Handler[T, ID]) DeleteTask(writer http.ResponseWriter, request *http.Request) {
	ctx, t := trace.NewTask(request.Context(), handler_pref+"DeleteTask")
	defer t.End()
	if id, ok, err := h.idRetriever(request); err != nil {
		internalErrOut(writer, "id", err)
	} else if !ok {
		badRequestOut(writer, "empty id", err)
	} else if ok, err := h.storage.Delete(ctx, id); err != nil {
		internalErrOut(writer, "storage", err)
	} else if !ok {
		writer.WriteHeader(http.StatusNotFound)
	} else {
		successResponse(ctx, writer)
	}
}

func writeJsonEntityResponse[T any](ctx context.Context, writer http.ResponseWriter, payload T) {
	defer trace.StartRegion(ctx, "writeResponse").End()
	if js, err := json.Marshal(payload); err != nil {
		internalErrOut(writer, "json-encode", err)
	} else {
		writeJsonResponse(writer, js)
	}
}

func writeJsonResponse(writer http.ResponseWriter, payload []byte) {
	writer.Header().Set("Content-Type", "application/json")
	writer.Write(payload)
}

type Status[ID any] struct {
	Id      ID   `json:"id,omitempty"`
	Success bool `json:"success"`
}

func successResponse(ctx context.Context, writer http.ResponseWriter) {
	writeJsonEntityResponse(ctx, writer, Status[any]{Success: true})
}

func decodeBody[T any](ctx context.Context, writer http.ResponseWriter, request *http.Request) (entity T, ok bool) {
	defer trace.StartRegion(ctx, "decodeBody").End()
	if err := json.NewDecoder(request.Body).Decode(&entity); err != nil {
		log.Errorf("json-decode: %s", err)
		badRequestOut(writer, "json-decode", err)
		return entity, false
	}
	return entity, true
}

func internalErrOut(writer http.ResponseWriter, name string, err error) {
	errOut(writer, name, err, http.StatusInternalServerError)
}

func badRequestOut(writer http.ResponseWriter, name string, err error) {
	errOut(writer, name, err, http.StatusBadRequest)
}

func errOut(writer http.ResponseWriter, name string, err error, statusCode int) {
	http.Error(writer, name+": "+err.Error(), statusCode)
	log.Errorf("errOut: %s: %s", name, err)
}
