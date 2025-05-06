package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.arc.lookup.LookupUnlessProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static m4gshm.benchmark.rest.quarkus.storage.StorageConfiguration.QUARKUS_HIBERNATE_ORM_ACTIVE;

@RequiredArgsConstructor
@ApplicationScoped
@Default
@LookupUnlessProperty(name = QUARKUS_HIBERNATE_ORM_ACTIVE, stringValue = "false", lookupIfMissing = true)
public class TaskStoragePanacheImpl implements Storage<TaskEntity, String> {
    private final TaskPanacheRepository taskRepo;
    private final TagPanacheRepository tagRepo;

    @Override
    public TaskEntity get(@NotNull String id) {
        return taskRepo.findById(id);
    }

    @NotNull
    @Override
    @Transactional
    public TaskEntity store(@NotNull TaskEntity entity) {
        var taskId = entity.getId();
        var tags = entity.getTags();
        if (tags == null || tags.isEmpty()) {
            tagRepo.deleteByTaskId(taskId);
        } else {
            tagRepo.deleteByTaskIdExcept(taskId, tags);
        }
        return taskRepo.getEntityManager().merge(entity);
    }

    @NotNull
    @Override
    public List<TaskEntity> getAll() {
        return taskRepo.listAll();
    }

    @Override
    @Transactional
    public boolean delete(@NotNull String id) {
        return taskRepo.deleteById(id);
    }
}
