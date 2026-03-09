package m4gshm.benchmark.rest.spring.boot;

import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.Task;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public abstract class TaskController<T extends Task> {

    private final Storage<T, String> storage;
    private final Status OK = new Status(true);

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<T> get(
            @PathVariable(value = "id") String id
    ) {
        var task = storage.get(id);
        return task == null ? notFound().build() : ok(task);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Collection<T> list(
    ) {
        return storage.getAll();
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public Status create(
            @RequestBody T task
    ) {
        storage.store(task, task.getId());
        return OK;
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Status update(
            @PathVariable("id") String id, @RequestBody T task
    ) {
        storage.store(task, id);
        return OK;
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Status delete(
            @PathVariable("id") String id
    ) {
        if (storage.delete(id)) {
            return OK;
        }
        throw new ResponseStatusException(NOT_FOUND);
    }

    public record Status(boolean success) {
    }

}
