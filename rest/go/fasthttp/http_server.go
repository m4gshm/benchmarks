package fasthttp

import (
	"net/http/pprof"

	"github.com/fasthttp/router"
	swagger "github.com/swaggo/http-swagger"
	"github.com/valyala/fasthttp"
	"github.com/valyala/fasthttp/fasthttpadaptor"

	"benchmark/rest/docs"
	"benchmark/rest/storage"
)

func NewTaskServer[T storage.IDAware[ID], ID comparable](addr string, storage storage.API[T, ID], idRetriever IdRetriever[ID], idGenerator IdGenerator[ID]) *fasthttp.Server {
	docs.SwaggerInfo.Host = addr

	handler := NewHandler(storage, idRetriever, idGenerator)
	r := router.New()

	// r.PanicHandler = func(ctx *fasthttp.RequestCtx, recoveryInfo interface{}) {
	// 	ctx.SetStatusCode(fasthttp.StatusInternalServerError)
	// 	ctx.SetBody([]byte(fmt.Sprintf("Unexpected error %v", recoveryInfo)))
	// }

	r.GET("/", fasthttpadaptor.NewFastHTTPHandler(swagger.WrapHandler))

	task := r.Group("/task")
	task.GET("/", handler.ListTasks)
	task.POST("/", handler.CreateTask)
	task.GET("/{id}", handler.GetTask)
	task.PUT("/{id}", handler.UpdateTask)
	task.DELETE("/{id}", handler.DeleteTask)

	dbg := r.Group("/debug/pprof")
	dbg.ANY("/", fasthttpadaptor.NewFastHTTPHandlerFunc(pprof.Index))
	dbg.ANY("/cmdline", fasthttpadaptor.NewFastHTTPHandlerFunc(pprof.Cmdline))
	dbg.ANY("/profile", fasthttpadaptor.NewFastHTTPHandlerFunc(pprof.Profile))
	dbg.ANY("/symbol", fasthttpadaptor.NewFastHTTPHandlerFunc(pprof.Symbol))
	dbg.ANY("/trace", fasthttpadaptor.NewFastHTTPHandlerFunc(pprof.Trace))

	return &fasthttp.Server{Handler: r.Handler}
}
