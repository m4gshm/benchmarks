package m4gshm.benchmark.rest.spring.boot;


import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.storage.MemoryStorageConfiguration;
import m4gshm.benchmark.storage.Storage;
import m4gshm.benchmark.storage.Task;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static m4gshm.benchmark.rest.spring.boot.TaskRestSpringWebflux.ROOT_PATH_TASK;
import static org.springframework.boot.SpringApplication.run;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.nativex.hint.TypeAccess.AUTO_DETECT;
import static org.springframework.nativex.hint.TypeAccess.DECLARED_FIELDS;
import static org.springframework.nativex.hint.TypeAccess.RESOURCE;
import static reactor.core.publisher.Flux.fromIterable;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.fromCallable;

@NativeHint(types = {
        @TypeHint(types = org.springdoc.core.Constants.class, access = {DECLARED_FIELDS, AUTO_DETECT, RESOURCE}),
        @TypeHint(types = io.swagger.v3.core.converter.ModelConverters.class, access = {DECLARED_FIELDS, AUTO_DETECT, RESOURCE}),
})
@SpringBootApplication
@Import(MemoryStorageConfiguration.class)
@RestController
@RequestMapping(ROOT_PATH_TASK)
@RequiredArgsConstructor
public class TaskRestSpringWebflux {

    public static final String ROOT_PATH_TASK = "/task";
    private static final Mono<Task> NOT_FOUND = error(new ResponseStatusException(HttpStatus.NOT_FOUND));
    private static final Status OK = new Status(true);
    private final Storage<Task, String> storage;

    public static void main(String[] args) {
        run(TaskRestSpringWebflux.class, args);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Mono<Task> get(@PathVariable(value = "id") String id) {
        return fromCallable(() -> storage.get(id)).switchIfEmpty(NOT_FOUND);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Flux<Task> list() {
        return fromIterable(storage.getAll());
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public Mono<Status> create(@RequestBody Task task) {
        return fromCallable(() -> {
            var id = task.getId();
            if (id == null) task.setId(id = UUID.randomUUID().toString());
            storage.store(id, task);
            return OK;
        });
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Mono<Status> update(@PathVariable("id") String id, @RequestBody Task task) {
        return fromCallable(() -> {
            if (task.getId() == null) {
                task.setId(id);
            }
            storage.store(id, task);
            return OK;
        });
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Mono<Status> delete(@PathVariable("id") String id) {
        return fromCallable(() -> {
            storage.delete(id);
            return OK;
        });
    }

    record Status(boolean success) {
    }
}
