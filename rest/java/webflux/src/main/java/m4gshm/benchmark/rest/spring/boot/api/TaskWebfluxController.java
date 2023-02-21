package m4gshm.benchmark.rest.spring.boot.api;


import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.impl.TaskImpl;
import m4gshm.benchmark.rest.spring.boot.service.ReactiveTaskService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static m4gshm.benchmark.rest.spring.boot.api.ReactiveTaskAPI.ROOT_PATH_TASK;


@ConditionalOnProperty(value = "controller", havingValue = "legacy")
@RestController
@RequestMapping(ROOT_PATH_TASK)
@RequiredArgsConstructor
public class TaskWebfluxController implements ReactiveTaskAPI<TaskImpl, TaskImpl, TaskImpl, LocalDateTime> {

    private final ReactiveTaskService<TaskImpl, LocalDateTime> service;

    @Override
    public Mono<TaskImpl> get(@PathVariable(value = "id") String id) {
        return service.get(id);
    }

    @Override
    public Flux<TaskImpl> list() {
        return service.list();
    }

    @Override
    public Mono<Status> create(@RequestBody TaskImpl task) {
        return service.create(task);
    }

    @Override
    public Mono<Status> update(@PathVariable("id") String id, @RequestBody TaskImpl task) {
        return service.update(id, task);
    }

    @Override
    public Mono<Status> delete(@PathVariable("id") String id) {
        return service.delete(id);
    }
}
