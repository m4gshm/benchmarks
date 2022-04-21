package m4gshm.benchmark.rest.spring.boot;


import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.model.Task;
import m4gshm.benchmark.storage.MapStorage;
import m4gshm.benchmark.storage.Storage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static m4gshm.benchmark.rest.spring.boot.ReactiveTaskAPI.ROOT_PATH_TASK;
import static org.springframework.boot.SpringApplication.run;
import static reactor.core.publisher.Flux.fromIterable;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.fromCallable;

@SpringBootApplication
@RestController
@RequestMapping(ROOT_PATH_TASK)
@RequiredArgsConstructor
public class TaskRestSpringWebflux implements ReactiveTaskAPI {
    private static final Mono<Task> NOT_FOUND = error(new ResponseStatusException(HttpStatus.NOT_FOUND));
    private static final Status OK = new Status(true);
    private final Storage<Task, String> storage = new MapStorage<>(new ConcurrentHashMap<>(1024, 0.75f, Runtime.getRuntime().availableProcessors()));

    public static void main(String[] args) {
        run(TaskRestSpringWebflux.class, args);
    }

    @Override
//    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Mono<Task> get(@PathVariable(value = "id") String id) {
        return fromCallable(() -> storage.get(id)).switchIfEmpty(NOT_FOUND);
    }

    @Override
//    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Flux<Task> list() {
        return fromIterable(storage.getAll());
    }

    @Override
//    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public Mono<Status> create(@RequestBody Task task) {
        return fromCallable(() -> {
            var id = task.getId();
            if (id == null) task.setId(id = UUID.randomUUID().toString());
            storage.store(id, task);
            return OK;
        });
    }

    @Override
//    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Mono<Status> update(@PathVariable("id") String id, @RequestBody Task task) {
        return fromCallable(() -> {
            if (task.getId() == null) {
                task.setId(id);
            }
            storage.store(id, task);
            return OK;
        });
    }

    @Override
//    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Mono<Status> delete(@PathVariable("id") String id) {
        return fromCallable(() -> {
            storage.delete(id);
            return OK;
        });
    }

}
