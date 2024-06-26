package http

import (
	"net/http"
	"net/http/pprof"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"github.com/go-chi/cors"
	"github.com/m4gshm/gollections/slice"
	swagger "github.com/swaggo/http-swagger"

	"benchmark/rest/docs"
	"benchmark/rest/storage"
)

func NewTaskServer[T storage.IDAware[ID], ID comparable](addr string, storage storage.API[T, ID], idRetriever IdRetriever[ID], idGenerator IdGenerator[ID]) *http.Server {
	// log.Println("listeining " + addr)
	docs.SwaggerInfo.Host = addr
	r := chi.NewRouter()

	r.Use(cors.Handler(cors.Options{
		AllowedOrigins: slice.Of("https://*", "http://*"),
		AllowedMethods: slice.Of("GET", "POST", "PUT", "DELETE", "OPTIONS"),
	}))

	r.Use(middleware.Recoverer)

	r.Mount("/", swagger.WrapHandler)

	handler := NewHandler(storage, idRetriever, idGenerator)
	r.Route("/task", func(r chi.Router) {
		r.Get("/{id}", handler.GetTask)
		r.Get("/", handler.ListTasks)
		r.Post("/", handler.CreateTask)
		r.Put("/{id}", handler.UpdateTask)
		r.Delete("/{id}", handler.DeleteTask)
	})

	r.HandleFunc("/debug/pprof/", pprof.Index)
	r.HandleFunc("/debug/pprof/cmdline", pprof.Cmdline)
	r.HandleFunc("/debug/pprof/profile", pprof.Profile)
	r.HandleFunc("/debug/pprof/symbol", pprof.Symbol)
	r.HandleFunc("/debug/pprof/trace", pprof.Trace)
	return &http.Server{Addr: addr, Handler: r}
}
