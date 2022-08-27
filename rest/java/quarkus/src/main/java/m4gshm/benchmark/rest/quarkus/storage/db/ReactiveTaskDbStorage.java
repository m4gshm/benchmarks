package m4gshm.benchmark.rest.quarkus.storage.db;


import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.MutinyStorage;
import m4gshm.benchmark.rest.java.storage.panache.TaskPanacheRepository;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.*;

@RequiredArgsConstructor
@ApplicationScoped
@IfBuildProperty(name = STORAGE, stringValue = STORAGE_VAL_DB)
@IfBuildProperty(name = REACTIVE, stringValue = "true", enableIfMissing = true)
public class ReactiveTaskDbStorage implements MutinyStorage<TaskEntity, String> {

    private final TaskPanacheRepository repository;

    @NotNull
    @Override
    public Uni<TaskEntity> get(@NotNull String id) {
        return repository.findById(id);
    }

    @NotNull
    @Override
    @ReactiveTransactional
    public Uni<TaskEntity> store(@NotNull TaskEntity entity) {
        return repository.persist(entity);
    }

    @NotNull
    @Override
    @ReactiveTransactional
    public Uni<TaskEntity> update(@NotNull TaskEntity entity) {
        return repository.getSession().flatMap(session -> session.merge(entity));
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