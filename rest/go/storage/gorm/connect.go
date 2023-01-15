package gorm

import (
	"errors"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

func NewConnect(dsn string, createBatchSize int, logLevel string) (*gorm.DB, error) {
	ll, err := getGormLogLevel(logLevel)
	if err != nil {
		return nil, err
	}
	return gorm.Open(postgres.New(postgres.Config{
		DSN: dsn,
	}), &gorm.Config{
		CreateBatchSize:                          createBatchSize,
		SkipDefaultTransaction:                   true,
		PrepareStmt:                              true,
		QueryFields:                              true,
		DisableForeignKeyConstraintWhenMigrating: true,
		Logger:                                   logger.Default.LogMode(ll),
	})
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
