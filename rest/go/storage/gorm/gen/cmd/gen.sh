#!/usr/bin/env bash

go run gen.go -dsn "host=localhost port=5433 user=postgres password=postgres dbname=postgres sslmode=disable client_encoding=UTF-8"