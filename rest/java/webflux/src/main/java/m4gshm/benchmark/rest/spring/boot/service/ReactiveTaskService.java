package m4gshm.benchmark.rest.spring.boot.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.jft.RestControllerEvent;
import m4gshm.benchmark.rest.java.storage.ReactorStorage;
import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;
import m4gshm.benchmark.rest.spring.boot.api.ReactiveTaskAPI;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

import static m4gshm.benchmark.rest.spring.boot.service.ReactiveTaskService.Properties.Scheduler.elastic;
import static reactor.core.publisher.Mono.*;

@Service
//@RequiredArgsConstructor
@EnableConfigurationProperties(ReactiveTaskService.Properties.class)
public class ReactiveTaskService<T extends Task<D> & IdAware<String> & WithId<T, String>, D> {
    private static final ReactiveTaskAPI.Status OK = new ReactiveTaskAPI.Status(true);
    private final Mono<T> NOT_FOUND = error(new ResponseStatusException(HttpStatus.NOT_FOUND));
    private final MonoSubscriber monoSubscriber;
    private final FluxSubscriber fluxSubscriber;
    private final ReactorStorage<T, String> storage;


    public ReactiveTaskService(ReactorStorage<T, String> storage, Properties properties
    ) {
        this.storage = storage;
        if (properties.scheduler == elastic) {
            var scheduler = Schedulers.newBoundedElastic(properties.size, Integer.MAX_VALUE, "task");
            monoSubscriber = new MonoSubscriber() {
                @Override
                public <T> Mono<T> apply(Mono<T> mono) {
                    return mono.subscribeOn(scheduler);
                }
            };
            fluxSubscriber = new FluxSubscriber() {
                @Override
                public <T> Flux<T> apply(Flux<T> flux) {
                    return flux.subscribeOn(scheduler);
                }
            };
        } else {
            monoSubscriber = new MonoSubscriber() {
                @Override
                public <T> Mono<T> apply(Mono<T> mono) {
                    return mono;
                }
            };
            fluxSubscriber = new FluxSubscriber() {
                @Override
                public <T> Flux<T> apply(Flux<T> flux) {
                    return flux;
                }
            };
        }
    }

    static <T> Mono<T> rec(String name, Mono<T> callable) {
        var event = RestControllerEvent.create(name);
        return callable.doOnSubscribe(subscription -> event.start()).doOnTerminate(event::finish);
    }

    static <T> Flux<T> rec(String name, Flux<T> callable) {
        var event = RestControllerEvent.create(name);
        return callable.doOnSubscribe(subscription -> event.start()).doOnTerminate(event::finish);
    }

    public Mono<T> get(String id) {
        return subscribe(rec("get", storage.get(id)).switchIfEmpty(NOT_FOUND));
    }

    public Flux<T> list() {
        return subscribe(rec("list", storage.getAll()));
    }

    public Mono<ReactiveTaskAPI.Status> create(T task) {
        return subscribe(rec("create", defer(() -> {
            var id = task.getId();
            var t = task;
            if (id == null) {
                t = task.withId(id = UUID.randomUUID().toString());
            }
            return storage.store(t).map(s -> OK);
        })));
    }

    public Mono<ReactiveTaskAPI.Status> update(String id, T task) {
        return subscribe(rec("update", defer(() -> {
            var t = task;
            if (task.getId() == null) {
                t = task.withId(id);
            }
            return storage.store(t).map(s -> OK);
        })));
    }

    public Mono<ReactiveTaskAPI.Status> delete(String id) {
        return subscribe(rec("delete", storage.delete(id).flatMap(deleted ->
                        deleted ? just(OK) : error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                )
        );
    }

    @NotNull
    private <T> Mono<T> subscribe(Mono<T> mono) {
        return monoSubscriber.apply(mono);
    }

    @NotNull
    private <T> Flux<T> subscribe(Flux<T> flux) {
        return fluxSubscriber.apply(flux);
    }

    @FunctionalInterface
    private interface MonoSubscriber {
        <T> Mono<T> apply(Mono<T> mono);
    }

    @FunctionalInterface
    private interface FluxSubscriber {
        <T> Flux<T> apply(Flux<T> flux);
    }

    @Data
    @RequiredArgsConstructor
    @ConstructorBinding
    @ConfigurationProperties("service.task.reactive")
    public static class Properties {
        private final Scheduler scheduler;
        private final int size;

        public enum Scheduler {
            none,
            elastic
        }
    }

}
