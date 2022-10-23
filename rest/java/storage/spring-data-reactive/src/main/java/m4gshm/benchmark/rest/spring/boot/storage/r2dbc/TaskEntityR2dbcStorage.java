package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.ReactorStorage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.fromCallable;
import static reactor.core.publisher.Mono.just;

@RequiredArgsConstructor
public class TaskEntityR2dbcStorage implements ReactorStorage<TaskEntity, String> {

    private final TaskEntityRepository taskEntityRepository;

    @Override
    public Mono<TaskEntity> get(String id) {
        return taskEntityRepository.findById(id);
    }

    @Override
    public Flux<TaskEntity> getAll() {
        return taskEntityRepository.findAll();
    }

    public Mono<TaskEntity> store(@NonNull TaskEntity entity) {
        var id = entity.getId();
        if (entity.isNew() || id == null) {
            return taskEntityRepository.save(entity);
        }
        return taskEntityRepository.findById(id).map(e -> entity).switchIfEmpty(just(entity.asNew())
                .flatMap(taskEntityRepository::save));
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return get(id).flatMap(e -> taskEntityRepository.delete(e)
                .map(v -> true)
                .switchIfEmpty(fromCallable(() -> true)))
                .defaultIfEmpty(false);
    }
}
