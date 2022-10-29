package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.arc.lookup.LookupUnlessProperty;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.transaction.Transactional;
import java.util.List;

import static m4gshm.benchmark.rest.quarkus.storage.StorageConfiguration.QUARKUS_HIBERNATE_ORM_ACTIVE;

@RequiredArgsConstructor
@ApplicationScoped
@Default
@LookupUnlessProperty(name = QUARKUS_HIBERNATE_ORM_ACTIVE, stringValue = "false", lookupIfMissing = true)
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
