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
	"syscall"
	"time"

	"benchmark/rest/http"
	"benchmark/rest/model"
	"benchmark/rest/storage"
	"benchmark/rest/storage/decorator"
	sgorm "benchmark/rest/storage/gorm"
	"benchmark/rest/storage/gorm/model"
	"benchmark/rest/storage/memory"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
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

	ctx := context.Background()

	exit := make(chan os.Signal, 1)
	signal.Notify(exit, os.Interrupt, syscall.SIGINT, syscall.SIGTERM, syscall.SIGKILL)

	log.Print("storage: ", *storageType)
	storage, err := initStorage(*storageType)
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
	log.Print("Server Exited Properly")
}

func initStorage(typ string) (storage storage.API[*model.Task, string], err error) {
	switch typ {
	case "memory":
		storage = memory.NewMemoryStorage[*model.Task, string]()
	case "gorm":
		var (
			db   *gorm.DB
			conn *sql.DB
			ll   logger.LogLevel
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
			if err = db.AutoMigrate(&task.Task{}, &task.Tag{}); err != nil {
				return
			}
		}

		storage = decorator.Warp[*task.Task, *model.Task, string](sgorm.NewRepository[*task.Task, string](db), task.ConvertToGorm, task.ConvertToDto)
		if conn, err = db.DB(); err != nil {
			return
		} else if err = conn.Ping(); err != nil {
			conn.Close()
			return
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
