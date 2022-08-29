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

	"benchmark/rest/storage"
	sgp "benchmark/rest/storage/gorm/postgres"
	"benchmark/rest/storage/memory"
	"benchmark/rest/task"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var (
	addr        = flag.String("addr", "localhost:8080", "listen address")
	storageType = flag.String("storage", "memory", "storage type; possible: memory, gorm")
	dsn         = flag.String("dsn", "host=localhost port=5432 user=postgres password=postgres dbname=postgres sslmode=disable", "Postgres dsn")
	logLevel    = flag.String("sql-log-level", "info", "SQL logger level")
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

	server := task.NewTaskServer(*addr, storage, task.StringID, task.UUIDGen)
	go func() { log.Fatal(server.ListenAndServe()) }()
	log.Print("server started")
	<-exit
	log.Print("server stopped")

	if err := server.Shutdown(ctx); err != nil {
		log.Fatalf("server shutdown failed:%+v", err)
	}
	log.Print("Server Exited Properly")
}

func initStorage(typ string) (storage storage.API[*task.Task, string], err error) {
	switch typ {
	case "memory":
		storage = memory.NewMemoryStorage[*task.Task, string]()
	case "gorm":
		var (
			db      *gorm.DB
			testCon *sql.DB
			ll      logger.LogLevel
		)
		ll, err = getGormLogLevel(*logLevel)
		if err != nil {
			return
		}
		db, err = gorm.Open(postgres.New(postgres.Config{
			DSN: *dsn,
		}), &gorm.Config{
			QueryFields: true,
			Logger:      logger.Default.LogMode(ll),
		})
		if err != nil {
			return
		}
		storage = sgp.NewRepository[*task.Task, string](db)
		if testCon, err = db.DB(); err != nil {
			return
		} else if err = testCon.Ping(); err != nil {
			testCon.Close()
			return
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
	// case "trace":
	// case "debug":
	case "info":
		return logger.Info, nil
	case "warn":
		return logger.Warn, nil
	case "error":
		return logger.Error, nil
	}
	return -1, errors.New("unsupported gorm log level " + levelCode)
}
