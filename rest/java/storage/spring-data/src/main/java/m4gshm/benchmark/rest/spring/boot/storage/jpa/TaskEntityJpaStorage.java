package m4gshm.benchmark.rest.spring.boot.storage.jpa;

import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;

import java.util.List;

@RequiredArgsConstructor
public class TaskEntityJpaStorage implements Storage<TaskEntity, String> {

    private final TaskEntityRepository taskEntityRepository;

    @Override
    public TaskEntity get(String id) {
        return taskEntityRepository.findById(id).orElse(null);
    }

    @Override
    public List<TaskEntity> getAll() {
        return (List<TaskEntity>) taskEntityRepository.findAll();
    }

    @Override
    public TaskEntity store(TaskEntity entity) {
        return taskEntityRepository.save(entity);
    }

    @Override
    public boolean delete(String id) {
        var found = get(id);
        if (found != null) {
            taskEntityRepository.delete(found);
            return true;
        }
        return false;
    }
}
