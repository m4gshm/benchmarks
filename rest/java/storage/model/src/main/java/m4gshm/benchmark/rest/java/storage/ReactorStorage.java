package m4gshm.benchmark.rest.java.storage;

import m4gshm.benchmark.rest.java.storage.model.IdAware;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReactorStorage<T extends IdAware<ID>, ID> {
    Mono<T> get(ID id);

    Flux<T> getAll();

    Mono<T> store(T entity);

    Mono<Boolean> delete(ID id);
}
