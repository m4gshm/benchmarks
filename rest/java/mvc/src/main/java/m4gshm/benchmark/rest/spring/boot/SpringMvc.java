package m4gshm.benchmark.rest.spring.boot;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.storage.Storage;
import m4gshm.benchmark.storage.Task;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.UUID;

import static org.springframework.boot.SpringApplication.run;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@SpringBootApplication(scanBasePackages = "m4gshm.benchmark")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class SpringMvc {
    private static final ResponseEntity<Task> notFound = notFound().build();
    private final Storage<Task, String> storage;
    private final Status OK = new Status(true);

    public static void main(String[] args) {
        run(SpringMvc.class, args);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Task> get(@PathVariable(value = "id") String id) {
        var task = storage.get(id);
        return task == null ? notFound : ok(task);
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
        storage.delete(id);
        return OK;
    }

    @Data
    public static class Status {
        private final boolean success;
    }
}
