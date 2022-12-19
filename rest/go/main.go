package main

import (
	"context"
	"database/sql"
	"errors"
	"flag"
	"fmt"
	"log"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/m4gshm/gollections/slice"
	sqldblogger "github.com/simukti/sqldb-logger"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"benchmark/rest/http"
	"benchmark/rest/model"
	"benchmark/rest/storage"
	"benchmark/rest/storage/decorator"
	sgorm "benchmark/rest/storage/gorm"
	task "benchmark/rest/storage/gorm/model"
	"benchmark/rest/storage/memory"
	ssql "benchmark/rest/storage/sql"
)

var (
	addr           = flag.String("addr", "localhost:8080", "listen address")
	storageType    = flag.String("storage", "memory", "storage type; possible: memory, gorm")
	dsn            = flag.String("dsn", "host=localhost port=5432 user=postgres password=postgres dbname=postgres sslmode=disable client_encoding=UTF-8", "Postgres dsn")
	logLevel       = flag.String("sql-log-level", "info", "SQL logger level")
	migrateDB      = flag.Bool("migrate-db", false, "apply automatic database migration")
	maxDbConns     = flag.Int("max-db-conns", -1, "Max DB connections")
	maxDbIdleConns = flag.Int("max-db-idle-conns", -1, "Max Idle DB connections")
	idleDbConnTime = flag.Duration("idle-db-conn-time", time.Minute, "Max DB connection itle time")
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
	signal.Notify(exit, os.Interrupt, syscall.SIGINT, syscall.SIGTERM, syscall.SIGKILL)

	log.Print("storage: ", *storageType)
	storage, err := initStorage(ctx, *storageType)
	if err != nil {
		log.Fatalf("storage init failed:%+v", err)
	}

	server := http.NewTaskServer(*addr, storage, http.StringID, http.UUIDGen)
	go func() { log.Fatal(server.ListenAndServe()) }()
	log.Print("server started at " + *addr)
	<-exit
	log.Print("server stopped")

	if err := server.Shutdown(ctx); err != nil {
		log.Fatalf("server shutdown failed:%+v", err)
	}

	shutdown()

	log.Print("Server Exited Properly")
}

func initStorage(ctx context.Context, typ string) (storage storage.API[*model.Task, string], err error) {
	switch typ {
	case "memory":
		storage = memory.NewMemoryStorage[*model.Task, string]()
	case "gorm":
		var (
			db *gorm.DB
			ll logger.LogLevel
		)
		ll, err = getGormLogLevel(*logLevel)
		if err != nil {
			return
		}
		db, err = gorm.Open(postgres.New(postgres.Config{
			DSN: *dsn,
		}), &gorm.Config{
			CreateBatchSize:                          10,
			SkipDefaultTransaction:                   true,
			PrepareStmt:                              true,
			QueryFields:                              true,
			DisableForeignKeyConstraintWhenMigrating: true,
			Logger:                                   logger.Default.LogMode(ll),
		})

		if err != nil {
			return
		}

		if migrateDB != nil && *migrateDB {
			migrator := db.Migrator()
			if err = migrator.AutoMigrate(&task.Task{}, &task.TaskTag{}); err != nil {
				return
			} else {
				if !migrator.HasConstraint(&task.Task{}, task.TaskFieldTags) {
					if err = migrator.CreateConstraint(&task.Task{}, task.TaskFieldTags); err != nil {
						return
					}
				}
			}
		}

		storage = decorator.Warp[*task.Task, *model.Task, string](sgorm.NewRepository[*task.Task, string](db), task.ConvertToGorm, task.ConvertToDto)
		var conn *sql.DB
		if conn, err = db.DB(); err != nil {
			return
		} else if err = initDBConnection(conn); err != nil {
			return
		}
		go func() {
			<-ctx.Done()
			log.Println("close gorm connection")
			if err := conn.Close(); err != nil {
				log.Println("close gorm connection err: " + err.Error())
			}
		}()
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
			if _, err := conn.Exec(`
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
				create index if not exists idx_task_tag_tag on task_tag (tag);
			`); err != nil {
				return nil, err
			}
		}

		storage = ssql.NewTaskRepository(conn)
		go func() {
			<-ctx.Done()
			log.Println("close pgx connection")
			if err := conn.Close(); err != nil {
				log.Println("close pgx err: " + err.Error())
			}
		}()
	default:
		err = errors.New("unsupported storage type " + typ)
	}

	return
}

func getGormLogLevel(levelCode string) (logger.LogLevel, error) {
	switch levelCode {
	case "off":
		return logger.Silent, nil
	case "silent":
		return logger.Silent, nil
	case "trace":
	case "debug":
	case "info":
		return logger.Info, nil
	case "warn":
		return logger.Warn, nil
	case "error":
		return logger.Error, nil
	}
	return -1, errors.New("unsupported gorm log level " + levelCode)
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
	}
	return nil
}

type SqlDBLogger struct {
}

func (SqlDBLogger) Log(ctx context.Context, level sqldblogger.Level, msg string, data map[string]interface{}) {
	switch msg {
	case "ExecContext":
		if query, ok := data["query"]; ok {
			logMsg := fmt.Sprintf("SQL: %s", query)
			if args, ok := data["args"]; ok {
				if aargs, ok := args.([]any); ok {
					sargs := slice.Map(aargs, func(a any) string {
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
