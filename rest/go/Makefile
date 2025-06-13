.PHONY: dep
dep:
	go install github.com/swaggo/swag/cmd/swag@v1.16.1
	go install github.com/m4gshm/fieldr@38f89cbdcf4ece57946a3e6119a0df76d447cae9

.PHONY: swag
swag:
	$(info generate swag)
	swag init

.PHONY: build
build: swag
	go generate ./...
	go build ./...

.PHONY: bin
bin: dep swag
ifeq ($(OS),Windows_NT)
	go build -o bin/server.exe main.go
else
	go build -o bin/server main.go
endif

.PHONY: run
run: swag
	$(info run app)
	go run main.go -storage sql -dsn "host=localhost port=5433 user=postgres password=postgres dbname=postgres sslmode=disable client_encoding=UTF-8"

.PHONY: test
test: build
	go clean -cache
	go test ./...

.PHONY: bench
bench: build
	go clean -cache
	go test -bench . -benchtime 5s -benchmem ./...
