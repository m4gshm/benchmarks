.PHONY: swag
swag:
	swag init

.PHONY: build
build: swag
	go build ./...

.PHONY: bin
bin: swag
	go build -o bin/server main.go	

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

