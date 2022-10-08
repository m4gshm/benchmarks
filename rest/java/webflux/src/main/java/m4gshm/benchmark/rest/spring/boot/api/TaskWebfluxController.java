package m4gshm.benchmark.rest.spring.boot.api;


import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import m4gshm.benchmark.rest.spring.boot.service.ReactiveTaskService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static m4gshm.benchmark.rest.spring.boot.api.ReactiveTaskAPI.ROOT_PATH_TASK;


@ConditionalOnProperty(value = "controller", havingValue = "webflux")
@RestController
@RequestMapping(ROOT_PATH_TASK)
@RequiredArgsConstructor
public class TaskWebfluxController implements ReactiveTaskAPI<TaskEntity, TaskEntity, TaskEntity, LocalDateTime> {

    private final ReactiveTaskService<TaskEntity, LocalDateTime> service;

    @Override
    public Mono<TaskEntity> get(@PathVariable(value = "id") String id) {
        return service.get(id);
    }

    @Override
    public Flux<TaskEntity> list() {
        return service.list();
    }

    @Override
    public Mono<Status> create(@RequestBody TaskEntity task) {
        return service.create(task);
    }

    @Override
    public Mono<Status> update(@PathVariable("id") String id, @RequestBody TaskEntity task) {
        return service.update(id, task);
    }

    @Override
    public Mono<Status> delete(@PathVariable("id") String id) {
        return service.delete(id);
    }
}
