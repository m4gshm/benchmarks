package test

import (
	"context"
	"log"
	"testing"
	"time"

	"github.com/m4gshm/gollections/convert/ptr"
	"github.com/m4gshm/gollections/slice"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/testcontainers/testcontainers-go"
	tlog "github.com/testcontainers/testcontainers-go/log"
	"github.com/testcontainers/testcontainers-go/modules/postgres"

	"benchmark/rest/db/connection"
	"benchmark/rest/model"
	"benchmark/rest/storage/decorator"
	sgorm "benchmark/rest/storage/gorm"
	gtask "benchmark/rest/storage/gorm/model"
)

var (
	dbName     = "postgres"
	dbUser     = "postgres"
	dbPassword = "postgres"
)

func NewPostgresContainer(ctx context.Context, dbName string, dbUser string, dbPassword string, t *testing.T) (*postgres.PostgresContainer, error) {
	opts := []testcontainers.ContainerCustomizer{
		postgres.WithDatabase(dbName),
		postgres.WithUsername(dbUser),
		postgres.WithPassword(dbPassword),
		postgres.BasicWaitStrategies(),
	}
	if t != nil {
		opts = append(opts, testcontainers.WithLogger(tlog.TestLogger(t)))
	}
	pc, err := postgres.Run(ctx, "postgres:17.5", opts...)
	return pc, err
}

func terminate(pc *postgres.PostgresContainer) {
	if pc != nil {
		if err := testcontainers.TerminateContainer(pc); err != nil {
			log.Printf("failed to terminate container: %s", err)
		}
	}
}

func Test_Gorm_Testcontainer_Postgres(t *testing.T) {
	ctx := context.Background()
	postgresContainer, err := NewPostgresContainer(ctx, dbName, dbUser, dbPassword, t)
	defer terminate(postgresContainer)
	require.NoError(t, err)

	str, err := postgresContainer.ConnectionString(ctx)
	require.NoError(t, err)

	db, err := connection.NewGormDB(ctx, str, 10, "trace", true)
	require.NoError(t, err)

	storage := decorator.Wrap(sgorm.NewRepository(db, (*gtask.Task).Save, gtask.DeleteByID), gtask.ConvertToGorm, gtask.ConvertToDto)

	id := "1"
	onSave := model.NewTask(id, "text", slice.Of("tag1", "tag2"), time.Now())
	stored, err := storage.Store(ctx, ptr.Of(onSave))
	require.NoError(t, err)

	require.NotNil(t, stored)
	require.Equal(t, onSave, *stored)

	loaded, ok, err := storage.Get(ctx, id)
	require.NoError(t, err)
	require.True(t, ok)

	assert.EqualExportedValues(t, onSave, *loaded)

	allLoaded, err := storage.List(ctx)
	require.NoError(t, err)
	require.Equal(t, 1, len(allLoaded))
	assert.EqualExportedValues(t, onSave, *allLoaded[0])

	ok, err = storage.Delete(ctx, id)
	require.NoError(t, err)
	require.True(t, ok)

	ok, err = storage.Delete(ctx, id)
	require.NoError(t, err)
	require.False(t, ok)
}
