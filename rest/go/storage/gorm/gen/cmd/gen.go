package main

import (
	"benchmark/rest/storage/gorm/model"
	"flag"
	"fmt"
	"log"
	"os"

	"gorm.io/gen"
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
		OutPath: "../query",
		Mode:    gen.WithDefaultQuery | gen.WithQueryInterface, // generate mode
	})

	g.ApplyBasic(model.Task{}, model.TaskTag{})
	g.Execute()
	return nil
}
