package task

import (
	"net/http"
	"net/http/pprof"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	swagger "github.com/swaggo/http-swagger"

	"benchmark/rest/docs"
	"benchmark/rest/storage"
)

func NewTaskServer[T storage.IDAware[ID], ID comparable](addr string, storage storage.API[T, ID], idRetriever IdRetriever[ID], idGenerator IdGenerator[ID]) *http.Server {
	// log.Println("listeining " + addr)
	docs.SwaggerInfo.Host = addr
	r := chi.NewRouter()
	// r.Use(middleware.RequestID)
	// r.Use(middleware.RealIP)
	// r.Use(middleware.Logger)
	r.Use(middleware.Recoverer)
	handler := NewHandler(storage, idRetriever, idGenerator)
	r.Route("/task", func(r chi.Router) {
		r.Get("/{id}", handler.GetTask)
		r.Get("/", handler.ListTasks)
		r.Post("/", handler.CreateTask)
		r.Put("/{id}", handler.UpdateTask)
		r.Delete("/{id}", handler.DeleteTask)
	})
	r.Mount("/swagger-ui", swagger.WrapHandler)

	r.HandleFunc("/debug/pprof/", pprof.Index)
	r.HandleFunc("/debug/pprof/cmdline", pprof.Cmdline)
	r.HandleFunc("/debug/pprof/profile", pprof.Profile)
	r.HandleFunc("/debug/pprof/symbol", pprof.Symbol)
	r.HandleFunc("/debug/pprof/trace", pprof.Trace)
	return &http.Server{Addr: addr, Handler: r}
}
