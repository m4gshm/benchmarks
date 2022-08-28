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
)

var (
	addr        = flag.String("addr", "localhost:8080", "listen address")
	storageType = flag.String("storage", "memory", "storage type; possible: memory, gorm")
	dsn         = flag.String("dsn", "host=localhost port=5432 user=postgres password=postgres dbname=postgres sslmode=disable", "Postgres dsn")
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
		)
		db, err = gorm.Open(postgres.New(postgres.Config{
			DSN: *dsn,
			// PreferSimpleProtocol: true, // disables implicit prepared statement usage
		}), &gorm.Config{
			QueryFields: true,
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
