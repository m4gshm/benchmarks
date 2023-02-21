package m4gshm.benchmark.rest.spring.boot.storage.r2dbc;

import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface TaskReactiveRepository<T extends TaskImpl>  /*extends R2dbcRepository<T, String>*/ {

    Mono<T> findById(String id);

    Mono<T> save(T entity);

    Mono<? extends Number> deleteById(String id);

    Flux<T> findAll();
}
