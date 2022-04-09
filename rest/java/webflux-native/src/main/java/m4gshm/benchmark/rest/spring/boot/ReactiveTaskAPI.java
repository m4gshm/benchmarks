package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.storage.Task;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface ReactiveTaskAPI {

    String ROOT_PATH_TASK = "/task";

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    Mono<Task> get(@PathVariable(value = "id") String id);

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    Flux<Task> list();

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    Mono<Status> create(@RequestBody Task task);

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    Mono<Status> update(@PathVariable("id") String id, @RequestBody Task task);

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    Mono<Status> delete(@PathVariable("id") String id);

    record Status(boolean success) {
    }
}
