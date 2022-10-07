package m4gshm.benchmark.rest.quarkus.storage.db;


import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;
import io.vertx.pgclient.PgException;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.MutinyStorage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.java.storage.panache.TaskPanacheRepository;
import org.hibernate.engine.spi.SelfDirtinessTracker;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.function.Function;

import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.*;

@RequiredArgsConstructor
@ApplicationScoped
@IfBuildProperty(name = STORAGE, stringValue = STORAGE_VAL_DB, enableIfMissing = true)
@IfBuildProperty(name = REACTIVE, stringValue = "true", enableIfMissing = true)
public class ReactiveTaskDbStorage implements MutinyStorage<TaskEntity, String> {

    public static final String DUPLICATED_KEY = "23505";
    private final TaskPanacheRepository repository;

    @Nullable
    private static String[] getDirtyAttributes(Object entity) {
        return entity instanceof SelfDirtinessTracker tracker && tracker.$$_hibernate_hasDirtyAttributes()
                ? tracker.$$_hibernate_getDirtyAttributes() : null;
    }

    @NotNull
    private static Function<Mutiny.Session, Uni<? extends TaskEntity>> merge(@NotNull TaskEntity entity) {
        return session -> session.withTransaction(t -> session.merge(entity));
    }

    private static <T> T restoreDirtyAttributes(T entity, String[] dirtyAttributes) {
        if (dirtyAttributes != null && entity instanceof SelfDirtinessTracker tracker) {
            for (var dirtyAttribute : dirtyAttributes) {
                tracker.$$_hibernate_trackChange(dirtyAttribute);
            }
        }
        return entity;
    }

    @NotNull
    @Override
    public Uni<TaskEntity> get(@NotNull String id) {
        return repository.findById(id);
    }

    @NotNull
    @Override
    public Uni<TaskEntity> store(@NotNull TaskEntity entity) {
        var dirtyAttributes = getDirtyAttributes(entity);
        var session = repository.getSession();
        return session.flatMap(merge(entity)).onFailure(this::isDuplicate).recoverWithUni(
                session.flatMap(merge(restoreDirtyAttributes(entity, dirtyAttributes)))
        );
    }

    private boolean isDuplicate(Throwable e) {
        var pgException = getPgException(e);
        return pgException != null && DUPLICATED_KEY.equals(pgException.getCode());
    }

    private PgException getPgException(Throwable e) {
        if (e instanceof PgException pgException) {
            return pgException;
        } else if (e == null) {
            return null;
        }
        var cause = e.getCause();
        if (cause == null || cause == e) {
            return null;
        }
        return getPgException(cause);
    }


    @NotNull
    @Override
    public Uni<List<TaskEntity>> getAll() {
        return repository.listAll();
    }

    @NotNull
    @Override
    @ReactiveTransactional
    public Uni<Boolean> delete(@NotNull String id) {
        return repository.deleteById(id);
    }
}