package m4gshm.benchmark.rest.spring.boot.api;

import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntityPersistable;
import m4gshm.benchmark.rest.spring.boot.api.ReactiveTaskAPI.Status;
import m4gshm.benchmark.rest.spring.boot.service.ReactiveTaskService;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static m4gshm.benchmark.rest.spring.boot.api.TaskAPI.ROOT_PATH_TASK;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@ConditionalOnProperty(value = "controller", havingValue = "router", matchIfMissing = true)
@RequiredArgsConstructor
@Configuration
public class RouterConfiguration {

    private final ReactiveTaskService<TaskEntityPersistable, LocalDateTime> service;

    @NotNull
    private static String id(ServerRequest req) {
        return req.pathVariable("id");
    }

    @NotNull
    private static Mono<TaskEntityPersistable> task(ServerRequest req) {
        return req.body(toMono(TaskEntityPersistable.class));
    }

    @NotNull
    private static Mono<ServerResponse> success(Status s) {
        return ok().body(fromValue(s));
    }

    @NotNull
    private static RequestPredicate json(RequestPredicate predicate) {
        return predicate.and(contentType(APPLICATION_JSON));
    }

    @Bean
    public RouterFunction<ServerResponse> restRouter() {
        return nest(
                path(ROOT_PATH_TASK),
                route(
                        GET("/{id}"), req -> ok().body(service.get(id(req)), Task.class)
                ).andRoute(
                        method(GET), req -> ok().body(service.list(), Task.class)
                ).andRoute(
                        json(method(POST)), req -> task(req).flatMap(task ->
                                service.create(task).flatMap(RouterConfiguration::success)
                        )
                ).andRoute(
                        json(PUT("/{id}")), req -> task(req).flatMap(task ->
                                service.update(id(req), task).flatMap(RouterConfiguration::success)
                        )
                ).andRoute(
                        DELETE("/{id}"), req ->
                                service.delete(id(req)).flatMap(RouterConfiguration::success)
                )
        );
    }
}
