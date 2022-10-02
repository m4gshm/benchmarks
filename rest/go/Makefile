.PHONY: swag
swag:
	go install github.com/swaggo/swag/cmd/swag@latest
	swag init

.PHONY: build
build: swag
	go build ./...

.PHONY: bin
bin: swag
ifeq ($(OS),Windows_NT)
	go build -o bin/server.exe main.go
else
	go build -o bin/server main.go
endif

.PHONY: run
run: swag
	go run ./...

.PHONY: test
test: build
	go clean -cache
	go test ./...

.PHONY: bench
bench: build
	go clean -cache
	go test -bench . -benchtime 5s -benchmem ./...

