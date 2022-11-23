package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.ReactorStorage;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntityPersistable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.fromCallable;
import static reactor.core.publisher.Mono.just;

@RequiredArgsConstructor
public class TaskEntityR2dbcStorage implements ReactorStorage<TaskEntityPersistable, String> {

    private final TaskEntityRepository<TaskEntityPersistable> taskEntityRepository;

    @Override
    public Mono<TaskEntityPersistable> get(String id) {
        return taskEntityRepository.findById(id);
    }

    @Override
    public Flux<TaskEntityPersistable> getAll() {
        return taskEntityRepository.findAll();
    }

    public Mono<TaskEntityPersistable> store(@NonNull TaskEntityPersistable entity) {
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
