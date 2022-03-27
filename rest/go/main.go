package main

import (
	"context"
	"log"
	"os"
	"os/signal"
	"syscall"

	"benchmark/rest/task"
)

// @title Task APIs
// @version 1.0
// @description Task APIs
// @BasePath /
func main() {
	ctx := context.Background()
	args := os.Args
	addr := "localhost:8080"
	if len(args) > 1 {
		addr = args[1]
	}

	exit := make(chan os.Signal, 1)
	signal.Notify(exit, os.Interrupt, syscall.SIGINT, syscall.SIGTERM, syscall.SIGKILL)

	server := task.NewTaskServer(addr, task.NewMemoryStorage())
	go func() {
		log.Fatal(server.ListenAndServe())
	}()
	log.Print("Server Started")
	<-exit
	log.Print("Server Stopped")

	if err := server.Shutdown(ctx); err != nil {
		log.Fatalf("Server Shutdown Failed:%+v", err)
	}
	log.Print("Server Exited Properly")
}
