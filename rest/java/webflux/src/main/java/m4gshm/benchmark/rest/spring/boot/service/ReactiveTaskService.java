package m4gshm.benchmark.rest.spring.boot.service;

import m4gshm.benchmark.rest.java.jft.RestControllerEvent;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;
import m4gshm.benchmark.rest.spring.boot.api.ReactiveTaskAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;
import java.util.concurrent.Callable;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.fromCallable;

@Service
//@RequiredArgsConstructor
public class ReactiveTaskService<T extends Task<D> & IdAware<String> & WithId<T, String>, D> {
    private static final ReactiveTaskAPI.Status OK = new ReactiveTaskAPI.Status(true);
    private final Mono<T> NOT_FOUND = error(new ResponseStatusException(HttpStatus.NOT_FOUND));
    private final Scheduler scheduler;
    private final Storage<T, String> storage;

    public ReactiveTaskService(Storage<T, String> storage, @Value("${service.task.reactive.pool.size}") int threadCap) {
        this.storage = storage;
        scheduler = Schedulers.newBoundedElastic(threadCap, Integer.MAX_VALUE, "task");
    }

    static <T> Callable<T> rec(String name, Callable<T> callable) {
        return () -> {
            try (var ignored = RestControllerEvent.start(name)) {
                return callable.call();
            }
        };
    }

    public Mono<T> get(String id) {
        return fromCallable(rec("get", () -> storage.get(id))).switchIfEmpty(NOT_FOUND).subscribeOn(scheduler);
    }

    public Flux<T> list() {
        return fromCallable(rec("list", storage::getAll)).flatMapIterable(tasks -> tasks).subscribeOn(scheduler);
    }

    public Mono<ReactiveTaskAPI.Status> create(T task) {
        return fromCallable(rec("create", () -> {
            var id = task.getId();
            var t = task;
            if (id == null) {
                t = task.withId(id = UUID.randomUUID().toString());
            }
            storage.store(t);
            return OK;
        })).subscribeOn(scheduler);
    }

    public Mono<ReactiveTaskAPI.Status> update(String id, T task) {
        return fromCallable(rec("update", () -> {
            var t = task;
            if (task.getId() == null) {
                t = task.withId(id);
            }
            storage.store(t);
            return OK;
        })).subscribeOn(scheduler);
    }

    public Mono<ReactiveTaskAPI.Status> delete(String id) {
        return fromCallable(rec("delete", () -> {
            if (storage.delete(id)) {
                return OK;
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        })).subscribeOn(scheduler);
    }

}
