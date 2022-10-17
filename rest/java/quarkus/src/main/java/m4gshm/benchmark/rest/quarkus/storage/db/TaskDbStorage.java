package m4gshm.benchmark.rest.quarkus.storage.db;

import io.quarkus.arc.properties.IfBuildProperty;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.STORAGE;
import static m4gshm.benchmark.rest.quarkus.BuildTimeProperties.STORAGE_VAL_DB;

@RequiredArgsConstructor
@ApplicationScoped
@IfBuildProperty(name = STORAGE, stringValue = STORAGE_VAL_DB, enableIfMissing = true)
public class TaskDbStorage implements Storage<TaskEntity, String> {
    private final TaskPanacheRepository repository;

    @Override
    public TaskEntity get(@NotNull String id) {
        return repository.findById(id);
    }

    @NotNull
    @Override
    @Transactional
    public TaskEntity store(@NotNull TaskEntity entity) {
        return repository.getEntityManager().merge(entity);
    }

    @NotNull
    @Override
    public List<TaskEntity> getAll() {
        return repository.listAll();
    }

    @Override
    @Transactional
    public boolean delete(@NotNull String id) {
        return repository.deleteById(id);
    }
}
