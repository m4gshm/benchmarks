package test

import (
	"context"
	"fmt"
	"log"
	"testing"
	"time"

	"github.com/m4gshm/gollections/convert/ptr"
	"github.com/m4gshm/gollections/slice"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/modules/postgres"

	"benchmark/rest/db/connection"
	"benchmark/rest/model"
	"benchmark/rest/storage/decorator"
	sgorm "benchmark/rest/storage/gorm"
	gtask "benchmark/rest/storage/gorm/model"
)

var postgresContainer *postgres.PostgresContainer

func TestMain(m *testing.M) {

	ctx := context.Background()

	dbName := "postgres"
	dbUser := "postgres"
	dbPassword := "postgres"

	pc, err := postgres.Run(ctx,
		"postgres:17.5",
		postgres.WithDatabase(dbName),
		postgres.WithUsername(dbUser),
		postgres.WithPassword(dbPassword),
		postgres.BasicWaitStrategies(),
	)
	postgresContainer = pc
	if postgresContainer != nil {
		defer func() {
			if err := testcontainers.TerminateContainer(postgresContainer); err != nil {
				log.Printf("failed to terminate container: %s", err)
			}
		}()
	}
	if err != nil {
		panic(err)
	}

	exitCode := m.Run()
	
	if exitCode != 0 {
		fmt.Printf("Some tests failed, exitCode: %d\n", exitCode)
	}
}

func Test_Gorm_Testcontainer_Postgres(t *testing.T) {
	ctx := context.Background()

	str, err := postgresContainer.ConnectionString(ctx)
	require.NoError(t, err)
	fmt.Println(str)

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
