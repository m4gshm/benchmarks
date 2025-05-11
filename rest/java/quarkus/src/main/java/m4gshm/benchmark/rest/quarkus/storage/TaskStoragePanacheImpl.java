package m4gshm.benchmark.rest.quarkus.storage;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class TaskStoragePanacheImpl implements Storage<TaskEntity, String> {
    private final TaskPanacheRepository taskRepo;
    private final TagPanacheRepository tagRepo;

    private static <T> T transactional(Callable<T> callable) {
        return QuarkusTransaction.joiningExisting().call(callable);
    }

    @Override
    public TaskEntity get(@NotNull String id) {
        return taskRepo.findById(id);
    }

    @NotNull
    @Override
    @Transactional
    public TaskEntity store(@NotNull TaskEntity entity, String id) {
        return transactional(() -> {
            var taskId = entity.getId();
            var tags = entity.getTags();
            if (tags == null || tags.isEmpty()) {
                tagRepo.deleteByTaskId(taskId);
            } else {
                tagRepo.deleteByTaskIdExcept(taskId, tags);
            }
            return taskRepo.getEntityManager().merge(entity);
        });
    }

    @NotNull
    @Override
    public List<TaskEntity> getAll() {
        return taskRepo.listAll();
    }

    @Override
    @Transactional
    public boolean delete(@NotNull String id) {
        return transactional(() -> taskRepo.deleteById(id));
    }
}
