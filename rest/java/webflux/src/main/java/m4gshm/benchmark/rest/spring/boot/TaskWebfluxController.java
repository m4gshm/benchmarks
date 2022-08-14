package m4gshm.benchmark.rest.spring.boot;


import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.jft.RestControllerEvent;
import m4gshm.benchmark.rest.java.model.Task;
import m4gshm.benchmark.storage.MapStorage;
import m4gshm.benchmark.storage.Storage;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import static m4gshm.benchmark.rest.spring.boot.ReactiveTaskAPI.ROOT_PATH_TASK;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.fromCallable;


@RestController
@RequestMapping(ROOT_PATH_TASK)
@RequiredArgsConstructor
public class TaskWebfluxController implements ReactiveTaskAPI {
    private static final Mono<Task> NOT_FOUND = error(new ResponseStatusException(HttpStatus.NOT_FOUND));
    private static final Status OK = new Status(true);
    private final Storage<Task, String> storage = new MapStorage<>(new ConcurrentHashMap<>(1024, 0.75f, Runtime.getRuntime().availableProcessors()));

    @NotNull
    private static <T> Callable<T> rec(String name, Callable<T> callable) {
        return () -> {
            try (var ignored = RestControllerEvent.start(name)) {
                return callable.call();
            }
        };
    }

    @Override
    public Mono<Task> get(@PathVariable(value = "id") String id) {
        return fromCallable(rec("get", () -> storage.get(id))).switchIfEmpty(NOT_FOUND);
    }

    @Override
    public Flux<Task> list() {
        return fromCallable(rec("list", storage::getAll)).flatMapIterable(tasks -> tasks);
    }

    @Override
    public Mono<Status> create(@RequestBody Task task) {
        return fromCallable(rec("create", () -> {
            var id = task.getId();
            if (id == null) task.setId(id = UUID.randomUUID().toString());
            storage.store(id, task);
            return OK;
        }));
    }

    @Override
    public Mono<Status> update(@PathVariable("id") String id, @RequestBody Task task) {
        return fromCallable(rec("update", () -> {
            if (task.getId() == null) {
                task.setId(id);
            }
            storage.store(id, task);
            return OK;
        }));
    }

    @Override
    public Mono<Status> delete(@PathVariable("id") String id) {
        return fromCallable(rec("delete", () -> {
            if (storage.delete(id)) {
                return OK;
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }));
    }
}
