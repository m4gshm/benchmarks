package m4gshm.benchmark.rest.spring.boot;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.model.Task;
import m4gshm.benchmark.storage.MapStorage;
import m4gshm.benchmark.storage.Storage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    private final Storage<Task, String> storage = new MapStorage<>(new ConcurrentHashMap<>(1024,
            0.75f, 100));
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
    public Status create(@RequestBody Task task) {
        var id = task.getId();
        if (id == null) task.setId(id = UUID.randomUUID().toString());
        storage.store(id, task);
        return OK;
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Status update(@PathVariable("id") String id, @RequestBody Task task) {
        if (task.getId() == null) {
            task.setId(id);
        }
        storage.store(id, task);
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
