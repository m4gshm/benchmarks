package fasthttp

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"runtime/trace"

	"github.com/valyala/fasthttp"

	_ "benchmark/rest/model"
	"benchmark/rest/storage"
)

func StringID(ctx *fasthttp.RequestCtx) (string, bool, error) {
	id := ctx.UserValue("id").(string)
	return id, len(id) > 0, nil
}

type IdRetriever[ID any] func(ctx *fasthttp.RequestCtx) (ID, bool, error)

type IdGenerator[ID any] func() (ID, error)

var _ IdRetriever[string] = StringID

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
func (h Handler[T, ID]) CreateTask(ctx *fasthttp.RequestCtx) {
	_, t := trace.NewTask(ctx, handler_pref+"CreateTask")
	defer t.End()
	if entity, ok := decodeBody[T](ctx); ok {
		var newId, noId ID
		if id := entity.GetId(); id == noId {
			if genId, err := h.idGenerator(); err != nil {
				internalErrOut(ctx, "idgen", err)
				return
			} else {
				entity.SetId(genId)
				newId = genId
			}
		}
		if ok := h.store(ctx, "create", entity); ok {
			writeJsonEntityResponse(ctx, Status[ID]{Id: newId, Success: true})
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
func (h Handler[T, ID]) UpdateTask(ctx *fasthttp.RequestCtx) {
	_, t := trace.NewTask(ctx, handler_pref+"UpdateTask")
	defer t.End()
	if entity, ok := decodeBody[T](ctx); ok {
		if id, ok, err := h.idRetriever(ctx); err != nil {
			internalErrOut(ctx, "id", err)
		} else if ok {
			entity.SetId(id)
		}
		if ok := h.store(ctx, "update", entity); ok {
			successResponse(ctx)
		}
	}
}

func (h Handler[T, ID]) store(ctx *fasthttp.RequestCtx, name string, entity T) (ok bool) {
	defer trace.StartRegion(ctx, name).End()
	trace.Log(ctx, "entityId", fmt.Sprint(entity.GetId()))
	if _, err := h.storage.Store(ctx, entity); err != nil {
		internalErrOut(ctx, name, err)
		return false
	}
	return true
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
func (h Handler[T, ID]) ListTasks(ctx *fasthttp.RequestCtx) {
	_, t := trace.NewTask(ctx, handler_pref+"ListTasks")
	defer t.End()
	if tasks, err := h.storage.List(ctx); err != nil {
		internalErrOut(ctx, "storage", err)
	} else {
		writeJsonEntityResponse(ctx, tasks)
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
func (h Handler[T, ID]) GetTask(ctx *fasthttp.RequestCtx) {
	_, t := trace.NewTask(ctx, handler_pref+"GetTask")
	defer t.End()
	if id, ok, err := h.idRetriever(ctx); err != nil {
		internalErrOut(ctx, "id", err)
	} else if !ok {
		ctx.Error("empty id", http.StatusBadRequest)
	} else if task, found, err := h.storage.Get(ctx, id); err != nil {
		internalErrOut(ctx, "storage", err)
	} else if found {
		writeJsonEntityResponse(ctx, task)
	} else {
		ctx.SetStatusCode(http.StatusNotFound)
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
func (h Handler[T, ID]) DeleteTask(ctx *fasthttp.RequestCtx) {
	_, t := trace.NewTask(ctx, handler_pref+"DeleteTask")
	defer t.End()
	if id, ok, err := h.idRetriever(ctx); err != nil {
		internalErrOut(ctx, "id", err)
	} else if !ok {
		ctx.Error("empty id", http.StatusBadRequest)
	} else if ok, err := h.storage.Delete(ctx, id); err != nil {
		internalErrOut(ctx, "storage", err)
	} else if !ok {
		ctx.SetStatusCode(http.StatusNotFound)
	} else {
		successResponse(ctx)
	}
}

func writeJsonEntityResponse[T any](ctx *fasthttp.RequestCtx, payload T) {
	defer trace.StartRegion(ctx, "writeResponse").End()
	if js, err := json.Marshal(payload); err != nil {
		internalErrOut(ctx, "json-encode", err)
	} else {
		writeJsonResponse(ctx, js)
	}
}

func writeJsonResponse(ctx *fasthttp.RequestCtx, payload []byte) {
	ctx.SetContentType("application/json")
	ctx.Write(payload)
}

type Status[ID any] struct {
	Id      ID   `json:"id,omitempty"`
	Success bool `json:"success"`
}

func successResponse(ctx *fasthttp.RequestCtx) {
	writeJsonEntityResponse(ctx, Status[any]{Success: true})
}

func decodeBody[T any](ctx *fasthttp.RequestCtx) (entity T, ok bool) {
	defer trace.StartRegion(ctx, "decodeBody").End()
	if err := json.NewDecoder(bytes.NewReader(ctx.Request.Body())).Decode(&entity); err != nil {
		errOut(ctx, "json-decode", err, http.StatusBadRequest)
		return entity, false
	}
	return entity, true
}

func internalErrOut(ctx *fasthttp.RequestCtx, name string, err error) {
	errOut(ctx, name, err, http.StatusInternalServerError)
}

func errOut(ctx *fasthttp.RequestCtx, name string, err error, statusCode int) {
	ctx.Error(name+": "+err.Error(), statusCode)
}
