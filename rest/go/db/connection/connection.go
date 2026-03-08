package connection

import (
	"context"
	"database/sql"
	"log"

	"gorm.io/gorm"

	sgorm "benchmark/rest/storage/gorm"
	gtask "benchmark/rest/storage/gorm/model"
)

const (
	initDBSQL = `
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

func NewGormDB(ctx context.Context, dsn string, createBatchSize int, logLevel string, migrateDB bool, opts ...func(*sql.DB) error) (db *gorm.DB, err error) {
	db, err = sgorm.NewConnect(dsn, createBatchSize, logLevel)

	if err != nil {
		return
	} else if migrateDB {
		if err = MigrateGormDB(db); err != nil {
			return
		}
	}

	var conn *sql.DB
	if conn, err = db.DB(); err != nil {
		return
	} else {
		for _, opt := range opts {
			if err = opt(conn); err != nil {
				return
			}
		}
	}
	closeConnOnCtxDone(ctx, "gorm", conn)
	return
}

func NewSqlDB(ctx context.Context, dsn string, migrate bool, opts ...func(*sql.DB) (*sql.DB, error)) (conn *sql.DB, err error) {
	if conn, err = sql.Open("pgx", dsn); err != nil {
		return
	} else {
		for _, opt := range opts {
			if conn, err = opt(conn); err != nil {
				return
			}
		}
	}
	if migrate {
		err = MigrateSql(ctx, conn)
	}
	closeConnOnCtxDone(ctx, "pgx", conn)
	return
}

func MigrateSql(ctx context.Context, conn *sql.DB) error {
	if _, err := conn.ExecContext(ctx, initDBSQL); err != nil {
		return err
	}
	return nil
}

func closeConnOnCtxDone(ctx context.Context, typeConn string, conn *sql.DB) {
	go func() {
		<-ctx.Done()
		log.Println("close " + typeConn + " connection")
		if err := conn.Close(); err != nil {
			log.Println("close " + typeConn + " connection err: " + err.Error())
		}
	}()
}

func MigrateGormDB(db *gorm.DB) error {
	migrator := db.Migrator()
	if err := migrator.AutoMigrate(&gtask.Task{}, &gtask.TaskTag{}); err != nil {
		return err
	} else if !migrator.HasConstraint(&gtask.Task{}, gtask.TaskFieldTags) {
		if err := migrator.CreateConstraint(&gtask.Task{}, gtask.TaskFieldTags); err != nil {
			return err
		}
	}
	return nil
}
