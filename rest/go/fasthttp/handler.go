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
	if entity, err := decodeBody[T](ctx); err == nil {
		var newId ID
		id := entity.GetId()
		var noId ID
		if id == noId {
			if newId, err = h.idGenerator(); err != nil {
				ctx.Error("idgen: "+err.Error(), http.StatusInternalServerError)
			} else {
				entity.SetId(newId)
			}
		}
		if err := h.store(ctx, "create", entity); err == nil {
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
	if entity, err := decodeBody[T](ctx); err == nil {
		if id, ok, err := h.idRetriever(ctx); err != nil {
			ctx.Error("id: "+err.Error(), http.StatusInternalServerError)
		} else if ok {
			entity.SetId(id)
		}
		if err := h.store(ctx, "update", entity); err == nil {
			successResponse(ctx)
		}
	}
}

func (h Handler[T, ID]) store(ctx *fasthttp.RequestCtx, name string, entity T) error {
	defer trace.StartRegion(ctx, name).End()
	trace.Log(ctx, "entityId", fmt.Sprint(entity.GetId()))
	if _, err := h.storage.Store(ctx, entity); err != nil {
		ctx.Error(name+": "+err.Error(), http.StatusInternalServerError)
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
func (h Handler[T, ID]) ListTasks(ctx *fasthttp.RequestCtx) {
	_, t := trace.NewTask(ctx, handler_pref+"ListTasks")
	defer t.End()
	if tasks, err := h.storage.List(ctx); err != nil {
		ctx.Error("storage: "+err.Error(), http.StatusInternalServerError)
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
		ctx.Error("id: "+err.Error(), http.StatusInternalServerError)
	} else if !ok {
		ctx.Error("empty id", http.StatusBadRequest)
	} else if task, found, err := h.storage.Get(ctx, id); err != nil {
		ctx.Error("storage: "+err.Error(), http.StatusInternalServerError)
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
		ctx.Error("id: "+err.Error(), http.StatusInternalServerError)
	} else if !ok {
		ctx.Error("empty id", http.StatusBadRequest)
	} else if ok, err := h.storage.Delete(ctx, id); err != nil {
		ctx.Error("storage: "+err.Error(), http.StatusInternalServerError)
	} else if !ok {
		ctx.SetStatusCode(http.StatusNotFound)
	} else {
		successResponse(ctx)
	}
}

func writeJsonEntityResponse(ctx *fasthttp.RequestCtx, payload interface{}) {
	defer trace.StartRegion(ctx, "writeResponse").End()
	if js, err := json.Marshal(payload); err != nil {
		ctx.Error("json-encode: "+err.Error(), http.StatusInternalServerError)
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

func decodeBody[T any](ctx *fasthttp.RequestCtx) (entity T, err error) {
	defer trace.StartRegion(ctx, "decodeBody").End()
	if err = json.NewDecoder(bytes.NewReader(ctx.Request.Body())).Decode(&entity); err != nil {
		ctx.Error("json-decode: "+err.Error(), http.StatusBadRequest)
	}
	return
}
