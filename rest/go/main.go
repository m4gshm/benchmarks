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
	"strings"
	"syscall"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/m4gshm/gollections/slice"
	sqldblogger "github.com/simukti/sqldb-logger"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"benchmark/rest/http"
	"benchmark/rest/model"
	"benchmark/rest/storage"
	"benchmark/rest/storage/decorator"
	sgorm "benchmark/rest/storage/gorm"
	gtask "benchmark/rest/storage/gorm/model"
	"benchmark/rest/storage/memory"
	ssql "benchmark/rest/storage/sql"
	sqltask "benchmark/rest/storage/sql/task"
)

var (
	addr            = flag.String("addr", "localhost:8080", "listen address")
	storageType     = flag.String("storage", "memory", "storage type; possible: memory, gorm")
	dsn             = flag.String("dsn", "host=localhost port=5432 user=postgres password=postgres dbname=postgres sslmode=disable client_encoding=UTF-8", "Postgres dsn")
	logLevel        = flag.String("sql-log-level", "info", "SQL logger level")
	migrateDB       = flag.Bool("migrate-db", false, "apply automatic database migration")
	maxDbConns      = flag.Int("max-db-conns", -1, "Max DB connections")
	maxDbIdleConns  = flag.Int("max-db-idle-conns", -1, "Max Idle DB connections")
	createBatchSize = flag.Int("gorm-create-batch-size", 1, "gorm CreateBatchSize param")
	idleDbConnTime  = flag.Duration("idle-db-conn-time", time.Minute, "Max DB connection itle time")
	initDBSQL       = `
	CREATE TABLE IF NOT EXISTS task
	(
		id       text NOT NULL,
		text     text,
		deadline timestamp without time zone,
		PRIMARY KEY (id)
	);
	CREATE TABLE IF NOT EXISTS task_tag
	(
		task_id text NOT NULL
			constraint fk_task_tags references task,
		tag     text NOT NULL,
		PRIMARY KEY (task_id, tag)
	);
`
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

	ctx, shutdown := context.WithCancel(context.Background())

	exit := make(chan os.Signal, 1)
	signal.Notify(exit, os.Interrupt, syscall.SIGINT, syscall.SIGTERM, syscall.SIGKILL)

	log.Print("storage: ", *storageType)
	storage, err := initStorage(ctx, *storageType)
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

	shutdown()

	log.Print("Server Exited Properly")
}

func initStorage(ctx context.Context, typ string) (storage storage.API[*model.Task, string], err error) {
	switch typ {
	case "memory":
		storage = memory.NewMemoryStorage[*model.Task, string]()
	case "gorm":
		var (
			db *gorm.DB
			ll logger.LogLevel
		)
		ll, err = getGormLogLevel(*logLevel)
		if err != nil {
			return
		}
		db, err = gorm.Open(postgres.New(postgres.Config{
			DSN: *dsn,
		}), &gorm.Config{
			CreateBatchSize:                          *createBatchSize,
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
			migrator := db.Migrator()
			if err = migrator.AutoMigrate(&gtask.Task{}, &gtask.TaskTag{}); err != nil {
				return
			} else {
				if !migrator.HasConstraint(&gtask.Task{}, gtask.TaskFieldTags) {
					if err = migrator.CreateConstraint(&gtask.Task{}, gtask.TaskFieldTags); err != nil {
						return
					}
				}
			}
		}

		storage = decorator.Warp[*gtask.Task, *model.Task, string](sgorm.NewRepository[*gtask.Task, string](db), gtask.ConvertToGorm, gtask.ConvertToDto)
		var conn *sql.DB
		if conn, err = db.DB(); err != nil {
			return
		} else if err = initDBConnection(conn); err != nil {
			return
		}
		go func() {
			<-ctx.Done()
			log.Println("close gorm connection")
			if err := conn.Close(); err != nil {
				log.Println("close gorm connection err: " + err.Error())
			}
		}()
	case "sql":
		var conn *sql.DB
		if conn, err = sql.Open("pgx", *dsn); err != nil {
			return
		} else if err = initDBConnection(conn); err != nil {
			return
		}
		if *logLevel != "silent" {
			conn = sqldblogger.OpenDriver(*dsn, conn.Driver(), SqlDBLogger{})
		}
		if migrateDB != nil && *migrateDB {
			if _, err := conn.Exec(initDBSQL); err != nil {
				return nil, err
			}
		}

		opts := &sql.TxOptions{}
		storage = ssql.NewRepository(
			func(ctx context.Context) (*sql.Tx, error) { return conn.BeginTx(ctx, opts) },
			func(ctx context.Context, t *sql.Tx) error { return t.Commit() },
			func(ctx context.Context, t *sql.Tx) error { return t.Rollback() },
			func(ctx context.Context, sql string, args ...any) (*sql.Rows, error) {
				return conn.QueryContext(ctx, sql, args...)
			},
			func(ctx context.Context, rows *sql.Rows) error { return rows.Close() },
			func(ctx context.Context, t *sql.Tx, sql string, args ...any) (sql.Result, error) {
				return t.ExecContext(ctx, sql, args...)
			},
			func(ctx context.Context, result sql.Result) (int64, error) { return result.RowsAffected() },
			sqltask.Delete[sql.Result, *sql.Tx],
			sqltask.Get[*sql.Rows],
			sqltask.List[*sql.Rows],
			sqltask.Store[*sql.Rows, sql.Result, *sql.Tx],
		)
		go func() {
			<-ctx.Done()
			log.Println("close pgx connection")
			if err := conn.Close(); err != nil {
				log.Println("close pgx err: " + err.Error())
			}
		}()
	case "pgx":
		var pool *pgxpool.Pool
		var config *pgxpool.Config
		config, err = pgxpool.ParseConfig(*dsn)
		if err != nil {
			return
		}
		if *maxDbConns >= 0 {
			config.MaxConns = (int32)(*maxDbConns)
		}
		if *maxDbIdleConns >= 0 {
			config.MinConns = (int32)(*maxDbIdleConns)
		}
		if *idleDbConnTime >= 0 {
			config.MaxConnIdleTime = *idleDbConnTime
		}
		pool, err = pgxpool.NewWithConfig(ctx, config)

		if err != nil {
			return
		}

		if migrateDB != nil && *migrateDB {
			if _, err := pool.Exec(ctx, initDBSQL); err != nil {
				return nil, err
			}
		}

		opts := pgx.TxOptions{}
		storage = ssql.NewRepository(
			func(ctx context.Context) (pgx.Tx, error) { return pool.BeginTx(ctx, opts) },
			func(ctx context.Context, t pgx.Tx) error { return t.Commit(ctx) },
			func(ctx context.Context, t pgx.Tx) error { return t.Rollback(ctx) },
			func(ctx context.Context, sql string, args ...any) (pgx.Rows, error) {
				return pool.Query(ctx, sql, args...)
			},
			func(ctx context.Context, rows pgx.Rows) error { rows.Close(); return nil },
			func(ctx context.Context, t pgx.Tx, sql string, args ...any) (pgconn.CommandTag, error) {
				return t.Exec(ctx, sql, args...)
			},
			func(ctx context.Context, result pgconn.CommandTag) (int64, error) { return result.RowsAffected(), nil },
			sqltask.Delete[pgconn.CommandTag, pgx.Tx],
			sqltask.Get[pgx.Rows],
			sqltask.List[pgx.Rows],
			sqltask.Store[pgx.Rows, pgconn.CommandTag, pgx.Tx],
		)

		return
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

func initDBConnection(conn *sql.DB) error {
	if err := conn.Ping(); err != nil {
		conn.Close()
		return err
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
	return nil
}

type SqlDBLogger struct {
}

func (SqlDBLogger) Log(ctx context.Context, level sqldblogger.Level, msg string, data map[string]interface{}) {
	switch msg {
	case "ExecContext", "QueryContext":
		if query, ok := data["query"]; ok {
			logMsg := fmt.Sprintf("SQL: %s", query)
			if args, ok := data["args"]; ok {
				if aargs, ok := args.([]any); ok {
					sargs := slice.Convert(aargs, func(a any) string {
						switch at := a.(type) {
						case *string:
							return fmt.Sprint(*at)
						}
						return fmt.Sprint(a)
					})
					logMsg += "\targs: " + strings.Join(sargs, ", ")
				}
			}
			log.Print(logMsg)
		}
	default:
		log.Print("SQL:" + msg)
	}
}
