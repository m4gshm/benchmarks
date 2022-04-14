package task

import (
	"encoding/json"
	"net/http"

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
	var task Task
	if err := json.NewDecoder(request.Body).Decode(&task); err != nil {
		http.Error(writer, "json-decode: "+err.Error(), http.StatusBadRequest)
	} else {
		_, err := h.storage.Store(request.Context(), &task)
		if err != nil {
			http.Error(writer, "storage: "+err.Error(), http.StatusInternalServerError)
		} else {
			writeJsonEntityResponse(writer, Status{Success: true})
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
// @Router       /task	[put]
func (h Handler) UpdateTask(writer http.ResponseWriter, request *http.Request) {
	var task Task
	if err := json.NewDecoder(request.Body).Decode(&task); err != nil {
		http.Error(writer, "json-decode: "+err.Error(), http.StatusBadRequest)
	} else {
		id := getId(request)
		if len(id) > 0 {
			task.Id = id
		}
		if _, err := h.storage.Store(request.Context(), &task); err != nil {
			http.Error(writer, "storage: "+err.Error(), http.StatusInternalServerError)
		} else {
			writeJsonEntityResponse(writer, Status{Success: true})
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
	tasks, err := h.storage.List(request.Context())
	if err != nil {
		http.Error(writer, "storage: "+err.Error(), http.StatusInternalServerError)
	} else {
		writeJsonEntityResponse(writer, tasks)
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
	task, err := h.storage.Get(request.Context(), getId(request))
	if err != nil {
		http.Error(writer, "storage: "+err.Error(), http.StatusInternalServerError)
	} else if task != nil {
		writeJsonEntityResponse(writer, task)
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
	if err := h.storage.Delete(request.Context(), getId(request)); err != nil {
		http.Error(writer, "storage: "+err.Error(), http.StatusInternalServerError)
	} else {
		writeJsonEntityResponse(writer, Status{Success: true})
	}
}

func getId(request *http.Request) string {
	id := chi.URLParam(request, "id")
	return id
}

func writeJsonEntityResponse(writer http.ResponseWriter, payload interface{}) {
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
