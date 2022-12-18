package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.ReactorStorage;
import m4gshm.benchmark.rest.spring.boot.storage.r2dbc.model.TaskEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TaskEntityR2dbcStorage implements ReactorStorage<TaskEntity, String> {

    private final TaskEntityRepository<TaskEntity> taskEntityRepository;

    @Override
    public Mono<TaskEntity> get(String id) {
        return taskEntityRepository.findById(id);
    }

    @Override
    public Flux<TaskEntity> getAll() {
        return taskEntityRepository.findAll();
    }

    public Mono<TaskEntity> store(@NonNull TaskEntity entity) {
        return taskEntityRepository.save(entity);
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return taskEntityRepository.deleteById(id).map(v -> v.intValue() != 0);
    }
}
