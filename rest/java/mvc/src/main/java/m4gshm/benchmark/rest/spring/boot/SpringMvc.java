package m4gshm.benchmark.rest.spring.boot;


import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.storage.MapStorage;
import m4gshm.benchmark.storage.Storage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.boot.SpringApplication.run;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@SpringBootApplication(scanBasePackages = "m4gshm.benchmark")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class SpringMvc {
    private final Storage<Task, String> storage = new MapStorage<>(new ConcurrentHashMap<>());
    private final Status OK = new Status(true);

    public static void main(String[] args) {
        run(SpringMvc.class, args);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Task> get(@PathVariable(value = "id") String id) {
        var task = storage.get(id);
        return task == null ? notFound().build() : ok(task);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Collection<Task> list() {
        return storage.getAll();
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public Status create(@RequestBody TaskEntity task) {
        var id = task.id();
        if (id == null) task = task.withId(id = UUID.randomUUID().toString());
        storage.store(task);
        return OK;
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Status update(@PathVariable("id") String id, @RequestBody TaskEntity task) {
        storage.store(task.withId(id));
        return OK;
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Status delete(@PathVariable("id") String id) {
        if (storage.delete(id)) {
            return OK;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public record Status(boolean success) {
    }
}
