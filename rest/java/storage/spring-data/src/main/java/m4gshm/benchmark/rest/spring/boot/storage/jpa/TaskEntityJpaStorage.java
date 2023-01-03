package m4gshm.benchmark.rest.spring.boot.storage.jpa;

import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TagEntity;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class TaskEntityJpaStorage implements Storage<TaskEntity, String> {

    private final TaskEntityRepository taskEntityRepository;
    private final TagEntityRepository tagEntityRepository;

    @Override
    public TaskEntity get(String id) {
        return taskEntityRepository.findById(id).orElse(null);
    }

    @Override
    public List<TaskEntity> getAll() {
        return (List<TaskEntity>) taskEntityRepository.findAll();
    }

    @Override
    @Transactional
    public TaskEntity store(TaskEntity entity) {
        var tagEntities = entity.getTagEntities();
        entity.setTagEntities(null);

        var taskId = entity.getId();
        if (tagEntities != null) {
            tagEntityRepository.deleteAllByTaskIdAndTagNotIn(
                    taskId, tagEntities.stream().map(TagEntity::getTag).collect(toSet())
            );
        }
        var stored = taskEntityRepository.save(entity);
        if (tagEntities != null) for (var tagEntity : tagEntities) {
            tagEntity.setTaskId(taskId);
            tagEntityRepository.save(tagEntity);
        }
        stored.setTagEntities(tagEntities);
        return stored;
    }

    @Override
    public boolean delete(String id) {
        var found = get(id);
        if (found != null) {
            tagEntityRepository.deleteAllByTaskId(id);
            taskEntityRepository.delete(found);
            return true;
        }
        return false;
    }
}
