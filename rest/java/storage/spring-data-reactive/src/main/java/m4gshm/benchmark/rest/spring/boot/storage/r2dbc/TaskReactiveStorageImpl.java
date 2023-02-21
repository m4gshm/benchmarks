package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.ReactorStorage;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TaskReactiveStorageImpl implements ReactorStorage<TaskImpl, String> {

    private final TaskReactiveRepository<TaskImpl> repository;

    @Override
    public Mono<TaskImpl> get(String id) {
        return repository.findById(id);
    }

    @Override
    public Flux<TaskImpl> getAll() {
        return repository.findAll();
    }

    public Mono<TaskImpl> store(@NonNull TaskImpl entity) {
        return repository.save(entity);
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return repository.deleteById(id).map(v -> v.intValue() != 0);
    }
}
