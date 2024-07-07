package main

import (
	"context"
	"database/sql"
	"errors"
	"flag"
	"fmt"
	"io/fs"
	"log"
	"net"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/grpc-ecosystem/grpc-gateway/v2/runtime"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/m4gshm/flag/flagenum"
	"github.com/m4gshm/gollections/expr/get"
	"github.com/m4gshm/gollections/op"
	"github.com/m4gshm/gollections/predicate/one"
	"github.com/m4gshm/gollections/slice"
	sqldblogger "github.com/simukti/sqldb-logger"
	swagger "github.com/swaggo/http-swagger"
	"github.com/swaggo/swag"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
	"gorm.io/gorm"

	"benchmark/rest/fasthttp"
	grpcTask "benchmark/rest/grpc/gen/go/task"
	implTask "benchmark/rest/grpc/impl/task"
	"benchmark/rest/grpc/static"
	benchHttp "benchmark/rest/http"
	"benchmark/rest/model"
	"benchmark/rest/storage"
	"benchmark/rest/storage/decorator"
	sgorm "benchmark/rest/storage/gorm"
	"benchmark/rest/storage/gorm/gen"
	gtask "benchmark/rest/storage/gorm/model"
	"benchmark/rest/storage/memory"
	ssql "benchmark/rest/storage/sql"
	sqltask "benchmark/rest/storage/sql/task"
)

const (
	ENGINE_HTTP              = "http"
	ENGINE_GRPC              = "grpc"
	ENGINE_GRPC_WITH_GATEWAY = "grpc-with-gateway"
)

var (
	addr            = flag.String("addr", "localhost:8080", "listen address")
	grpcAddr        = flag.String("grpc-addr", "localhost:9090", "grpc listen address")
	storageType     = flag.String("storage", "memory", "storage type; possible: memory, gorm")
	engine          = flagenum.SingleString("engine", ENGINE_HTTP, slice.Of(ENGINE_HTTP, ENGINE_GRPC, ENGINE_GRPC_WITH_GATEWAY), "server engine")
	dsn             = flag.String("dsn", "host=localhost port=5432 user=postgres password=postgres dbname=postgres sslmode=disable client_encoding=UTF-8", "Postgres dsn")
	logLevel        = flag.String("sql-log-level", "info", "SQL logger level")
	migrateDB       = flag.Bool("migrate-db", false, "apply automatic database migration")
	maxDbConns      = flag.Int("max-db-conns", -1, "Max DB connections")
	maxDbIdleConns  = flag.Int("max-db-idle-conns", 1, "Max Idle DB connections")
	createBatchSize = flag.Int("gorm-create-batch-size", 1, "gorm CreateBatchSize param")
	idleDbConnTime  = flag.Duration("idle-db-conn-time", time.Minute, "Max DB connection itle time")
	maxDbConnTime   = flag.Duration("max-db-conn-time", time.Minute*10, "Max DB connection time")
	fastHttp        = flag.Bool("fast-http", false, "Use fast http router")
	initDBSQL       = `
	CREATE TABLE IF NOT EXISTS task
	(
		id       text NOT NULL,
		text     text,
		deadline timestamp without time zone,
		PRIMARY KEY (id)
	);
	CREATE TABLE IF NOT EXISTS task_tag
	(
		task_id text NOT NULL
			constraint fk_task_tags references task,
		tag     text NOT NULL,
		PRIMARY KEY (task_id, tag)
	);
`
)

func usage() {
	_, _ = fmt.Fprintf(os.Stderr, "Flags:\n")
	flag.PrintDefaults()
}

// @title Task APIs
// @version 1.0
// @description Task APIs
// @BasePath /
func main() {
	flag.Usage = usage
	flag.Parse()

	ctx, shutdown := context.WithCancel(context.Background())

	exit := make(chan os.Signal, 1)
	signal.Notify(exit, os.Interrupt, syscall.SIGINT, syscall.SIGTERM)

	log.Print("storage: ", *storageType)
	if storage, err := initStorage(ctx, *storageType); err != nil {
		log.Fatalf("storage init failed:%+v", err)
	} else if isGrpc := one.Of(ENGINE_GRPC, ENGINE_GRPC_WITH_GATEWAY); isGrpc(*engine) {
		log.Print("grpc")

		taskService := &implTask.TaskServiceServerIml{Storage: storage}
		grpcServer := grpc.NewServer()
		grpcTask.RegisterTaskServiceServer(grpcServer, taskService)
		reflection.Register(grpcServer)

		lis, err := net.Listen("tcp", *grpcAddr)
		if err != nil {
			log.Fatalf("failed to listen: %v", err)
		}
		go func() {
			if err := grpcServer.Serve(lis); err != nil {
				log.Fatalf("failed to serve grpc server: %v", err)
			}
		}()
		log.Printf("grpc requests listening at %v", lis.Addr())

		var httpServer *http.Server
		if *engine == ENGINE_GRPC_WITH_GATEWAY {
			httpServer = newGrpcRestGateway(ctx, taskService)
			go func() {
				if err := httpServer.ListenAndServe(); err != nil {
					log.Fatalf("failed to serve http gateway: %v", err)
				}
			}()
			log.Printf("http requests listening at %v", *addr)
		}
		<-exit
		grpcServer.Stop()
		if httpServer != nil {
			if err := httpServer.Shutdown(ctx); err != nil {
				log.Fatalf("http server shutdown failed:%+v", err)
			}
		}
		log.Print("server stopped")
	} else if *fastHttp {
		log.Print("fast http")
		server := fasthttp.NewTaskServer(*addr, storage, fasthttp.StringID, benchHttp.UUIDGen)
		go func() { log.Fatal(server.ListenAndServe(*addr)) }()
		log.Print("server started at " + *addr)
		<-exit
		log.Print("server stopped")

		if err := server.Shutdown(); err != nil {
			log.Fatalf("server shutdown failed:%+v", err)
		}
	} else {
		log.Print("http")
		server := benchHttp.NewTaskServer(*addr, storage, benchHttp.StringID, benchHttp.UUIDGen)
		go func() { log.Fatal(server.ListenAndServe()) }()
		log.Print("server started at " + *addr)
		<-exit
		log.Print("server stopped")

		if err := server.Shutdown(ctx); err != nil {
			log.Fatalf("server shutdown failed:%+v", err)
		}
	}

	shutdown()

	log.Print("Server Exited Properly")
}

func newGrpcRestGateway(ctx context.Context, taskService *implTask.TaskServiceServerIml) *http.Server {
	name := "grpc"
	swaggerHandler := swagger.Handler(func(c *swagger.Config) { c.InstanceName = name })

	openapiJson, err := readSwaggerJson()
	if err != nil {
		log.Fatalf("couldn't read swagger json file: %v ", err)
	}
	swag.Register(name, &swag.Spec{SwaggerTemplate: string(openapiJson)})

	mux := runtime.NewServeMux()
	if err := grpcTask.RegisterTaskServiceHandlerServer(ctx, mux, taskService); err != nil {
		log.Fatalf("failed to serve gRPC gateway: %v", err)
	}

	httpServer := &http.Server{
		Addr: *addr,
		Handler: http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			op.IfElse(strings.HasPrefix(r.URL.Path, "/api"), mux.ServeHTTP, swaggerHandler)(w, r)
		}),
	}
	return httpServer
}

func readSwaggerJson() ([]byte, error) {
	subFS, err := fs.Sub(static.SwaggerJson, ".")
	if err != nil {
		return nil, err
	}
	return fs.ReadFile(subFS, "apidocs.swagger.json")
}

func initStorage(ctx context.Context, typ string) (storage storage.API[*model.Task, string], err error) {
	log.Println("connection: " + strings.Join(slice.Convert(strings.Split(*dsn, " "), func(prop string) string {
		pair := strings.Split(prop, "=")
		return get.If(len(pair) > 1 && pair[0] == "password", func() string { return pair[0] + "=******" }).Else(prop)
	}), " "))
	switch typ {
	case "memory":
		storage = memory.NewMemoryStorage[*model.Task, string]()
	case "gorm":
		db, err := NewGormDB(ctx, *dsn, *createBatchSize, *logLevel, *migrateDB)
		if err != nil {
			return nil, err
		}
		storage = decorator.Wrap[*gtask.Task, *model.Task, string](sgorm.NewRepository(db, (*gtask.Task).Save, gtask.DeleteByID), gtask.ConvertToGorm, gtask.ConvertToDto)
	case "gorm-gen":
		db, err := NewGormDB(ctx, *dsn, *createBatchSize, *logLevel, *migrateDB)
		if err != nil {
			return nil, err
		}
		storage = decorator.Wrap[*gtask.Task, *model.Task, string](gen.NewRepository(db), gtask.ConvertToGorm, gtask.ConvertToDto)
	case "sql":
		var conn *sql.DB

		if conn, err = sql.Open("pgx", *dsn); err != nil {
			return
		} else if err = initDBConnection(conn); err != nil {
			return
		}
		if *logLevel != "silent" {
			conn = sqldblogger.OpenDriver(*dsn, conn.Driver(), SqlDBLogger{})
		}
		if migrateDB != nil && *migrateDB {
			if err = migrateSql(ctx, conn); err != nil {
				return
			}
		}
		storage = newSqlStorage(conn)
		closeConnOnCtxDone(ctx, "pgx", conn)
	case "pgx":
		var config *pgxpool.Config
		if config, err = pgxpool.ParseConfig(*dsn); err != nil {
			return
		}
		if *maxDbConns >= 0 {
			config.MaxConns = (int32)(*maxDbConns)
		}
		if *maxDbIdleConns >= 0 {
			config.MinConns = (int32)(*maxDbIdleConns)
		}
		if *idleDbConnTime >= 0 {
			config.MaxConnIdleTime = *idleDbConnTime
		}
		if *maxDbConnTime >= 0 {
			config.MaxConnLifetime = *maxDbConnTime
		}
		var pool *pgxpool.Pool
		if pool, err = pgxpool.NewWithConfig(ctx, config); err != nil {
			return
		} else if migrateDB != nil && *migrateDB {
			if err = migratePgx(ctx, pool); err != nil {
				return
			}
		}
		storage = newPgxStorage(pool)
	default:
		err = errors.New("unsupported storage type " + typ)
	}
	return
}

func migratePgx(ctx context.Context, pool *pgxpool.Pool) error {
	if _, err := pool.Exec(ctx, initDBSQL); err != nil {
		return err
	}
	return nil
}

func migrateSql(ctx context.Context, conn *sql.DB) error {
	if _, err := conn.ExecContext(ctx, initDBSQL); err != nil {
		return err
	}
	return nil
}

func newSqlStorage(conn *sql.DB) storage.API[*model.Task, string] {
	opts := &sql.TxOptions{}
	return ssql.NewRepository(
		func(ctx context.Context) (*sql.Tx, error) { return conn.BeginTx(ctx, opts) },
		func(ctx context.Context, t *sql.Tx) error { return t.Commit() },
		func(ctx context.Context, t *sql.Tx) error { return t.Rollback() },
		func(ctx context.Context, sql string, args ...any) (*sql.Rows, error) {
			return conn.QueryContext(ctx, sql, args...)
		},
		func(ctx context.Context, rows *sql.Rows) error { return rows.Close() },
		func(ctx context.Context, t *sql.Tx, sql string, args ...any) (sql.Result, error) {
			return t.ExecContext(ctx, sql, args...)
		},
		func(ctx context.Context, result sql.Result) (int64, error) { return result.RowsAffected() },
		sqltask.Delete[sql.Result, *sql.Tx],
		sqltask.Get[*sql.Rows],
		sqltask.List[*sql.Rows],
		sqltask.Store[*sql.Rows, sql.Result, *sql.Tx],
	)
}

func newPgxStorage(pool *pgxpool.Pool) storage.API[*model.Task, string] {
	opts := pgx.TxOptions{}
	return ssql.NewRepository(
		func(ctx context.Context) (pgx.Tx, error) { return pool.BeginTx(ctx, opts) },
		func(ctx context.Context, t pgx.Tx) error { return t.Commit(ctx) },
		func(ctx context.Context, t pgx.Tx) error { return t.Rollback(ctx) },
		func(ctx context.Context, sql string, args ...any) (pgx.Rows, error) {
			return pool.Query(ctx, sql, args...)
		},
		func(ctx context.Context, rows pgx.Rows) error { rows.Close(); return nil },
		func(ctx context.Context, t pgx.Tx, sql string, args ...any) (pgconn.CommandTag, error) {
			return t.Exec(ctx, sql, args...)
		},
		func(ctx context.Context, result pgconn.CommandTag) (int64, error) { return result.RowsAffected(), nil },
		sqltask.Delete[pgconn.CommandTag, pgx.Tx],
		sqltask.Get[pgx.Rows],
		sqltask.List[pgx.Rows],
		sqltask.Store[pgx.Rows, pgconn.CommandTag, pgx.Tx],
	)
}

func NewGormDB(ctx context.Context, dsn string, createBatchSize int, logLevel string, migrateDB bool) (db *gorm.DB, err error) {
	db, err = sgorm.NewConnect(dsn, createBatchSize, logLevel)

	if err != nil {
		return
	} else if migrateDB {
		if err = MigrateGormDB(db); err != nil {
			return
		}
	}

	var conn *sql.DB
	if conn, err = db.DB(); err != nil {
		return
	} else if err = initDBConnection(conn); err != nil {
		return
	}

	closeConnOnCtxDone(ctx, "gorm", conn)

	return
}

func closeConnOnCtxDone(ctx context.Context, typeConn string, conn *sql.DB) {
	go func() {
		<-ctx.Done()
		log.Println("close " + typeConn + " connection")
		if err := conn.Close(); err != nil {
			log.Println("close " + typeConn + " connection err: " + err.Error())
		}
	}()
}

func MigrateGormDB(db *gorm.DB) error {
	migrator := db.Migrator()
	if err := migrator.AutoMigrate(&gtask.Task{}, &gtask.TaskTag{}); err != nil {
		return err
	} else if !migrator.HasConstraint(&gtask.Task{}, gtask.TaskFieldTags) {
		if err := migrator.CreateConstraint(&gtask.Task{}, gtask.TaskFieldTags); err != nil {
			return err
		}
	}
	return nil
}

func initDBConnection(conn *sql.DB) error {
	if err := conn.Ping(); err != nil {
		conn.Close()
		return err
	} else {
		if *maxDbConns >= 0 {
			conn.SetMaxOpenConns(*maxDbConns)
		}
		if *maxDbIdleConns >= 0 {
			conn.SetMaxIdleConns(*maxDbIdleConns)
		}
		if *idleDbConnTime >= 0 {
			conn.SetConnMaxIdleTime(*idleDbConnTime)
		}
		if *maxDbConnTime >= 0 {
			conn.SetConnMaxIdleTime(*maxDbConnTime)
		}
	}
	return nil
}

type SqlDBLogger struct {
}

func (SqlDBLogger) Log(ctx context.Context, level sqldblogger.Level, msg string, data map[string]interface{}) {
	switch msg {
	case "ExecContext", "QueryContext":
		if query, ok := data["query"]; ok {
			logMsg := fmt.Sprintf("SQL: %s", query)
			if args, ok := data["args"]; ok {
				if aargs, ok := args.([]any); ok {
					sargs := slice.Convert(aargs, func(a any) string {
						switch at := a.(type) {
						case *string:
							return fmt.Sprint(*at)
						}
						return fmt.Sprint(a)
					})
					logMsg += "\targs: " + strings.Join(sargs, ", ")
				}
			}
			log.Print(logMsg)
		}
	default:
		log.Print("SQL:" + msg)
	}
}
