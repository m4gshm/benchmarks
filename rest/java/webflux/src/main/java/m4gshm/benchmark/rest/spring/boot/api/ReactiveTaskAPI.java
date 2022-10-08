package m4gshm.benchmark.rest.spring.boot.api;

import m4gshm.benchmark.rest.java.storage.model.Task;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface ReactiveTaskAPI<T extends Task<D>, C extends T, U extends T, D> {

    String ROOT_PATH_TASK = "/task";

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    Mono<T> get(@PathVariable(value = "id") String id);

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    Flux<T> list();

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    Mono<Status> create(@RequestBody C task);

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    Mono<Status> update(@PathVariable("id") String id, @RequestBody U task);

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    Mono<Status> delete(@PathVariable("id") String id);

    record Status(Boolean success) {
    }
}
