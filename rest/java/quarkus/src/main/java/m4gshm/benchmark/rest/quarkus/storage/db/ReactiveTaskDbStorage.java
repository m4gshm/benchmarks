package m4gshm.benchmark.rest.quarkus.storage.db;


import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniOnFailure;
import io.vertx.pgclient.PgException;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.MutinyStorage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.java.storage.panache.TaskPanacheRepository;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.*;

@RequiredArgsConstructor
@ApplicationScoped
@IfBuildProperty(name = STORAGE, stringValue = STORAGE_VAL_DB, enableIfMissing = true)
@IfBuildProperty(name = REACTIVE, stringValue = "true", enableIfMissing = true)
public class ReactiveTaskDbStorage implements MutinyStorage<TaskEntity, String> {

    public static final String DUPLICATED_KEY = "23505";
    private final TaskPanacheRepository repository;

    @NotNull
    @Override
    public Uni<TaskEntity> get(@NotNull String id) {
        return repository.findById(id);
    }

    @NotNull
    @Override
//    @ReactiveTransactional
    public Uni<TaskEntity> store(@NotNull TaskEntity entity) {
//        return repository.getSession().flatMap(session->session.withTransaction(t->session.merge(entity)));
        return repository.getSession().flatMap(session -> {
            Uni<TaskEntity> onOnflict = repository.getSession().flatMap(session2 -> session2.flush().map(v -> session2)).flatMap(session2 -> {

                TaskEntity reference = session2.getReference(entity);

                Mutiny.Session detach = session2.detach(reference).clear();
                return detach.flush().flatMap(v -> detach.withTransaction(t -> detach.merge(reference)));
            });
            UniOnFailure<TaskEntity> taskEntityUniOnFailure = session.withTransaction(t -> session.persist(entity)).map(v -> entity).onFailure(this::isDuplicate);
            return taskEntityUniOnFailure.recoverWithUni(onOnflict);
        });

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