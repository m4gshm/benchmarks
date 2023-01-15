package main

import (
	"benchmark/rest/storage/gorm"
	"benchmark/rest/storage/gorm/model"
	"flag"
	"fmt"
	"log"
	"os"

	"gorm.io/gen"
)

// Dynamic SQL
// type Querier interface {
// 	// SELECT * FROM @@table WHERE id=@id
// 	GetByID(id string) (gen.T, error)
// }

var (
	dsn             = flag.String("dsn", "host=localhost port=5432 user=postgres password=postgres dbname=postgres sslmode=disable client_encoding=UTF-8", "Postgres dsn")
	logLevel        = flag.String("sql-log-level", "info", "SQL logger level")
	createBatchSize = flag.Int("gorm-create-batch-size", 1, "gorm CreateBatchSize param")
)

func usage() {
	_, _ = fmt.Fprintf(os.Stderr, "Flags:\n")
	flag.PrintDefaults()
}

func main() {
	if err := run(); err != nil {
		log.Fatalf("run failed:%v", err)
	}
}

func run() error {
	flag.Usage = usage
	flag.Parse()
	g := gen.NewGenerator(gen.Config{
		OutPath: "query",
		Mode:    gen.WithoutContext | gen.WithDefaultQuery | gen.WithQueryInterface, // generate mode
	})

	db, err := gorm.NewConnect(*dsn, *createBatchSize, *logLevel)
	if err != nil {
		return err
	}

	g.UseDB(db)
	g.ApplyBasic(model.Task{}, model.TaskTag{})
	// g.ApplyInterface(func(Querier) {}, model.Task{}, model.TaskTag{})
	g.Execute()
	return nil
}
