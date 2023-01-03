package m4gshm.benchmark.rest.spring.boot;


import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.jfr.HttpEvent;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collection;

import static m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity.initId;
import static m4gshm.benchmark.rest.spring.boot.EventFilter.JFR_HTTP_REQUEST_EVENT;
import static org.springframework.boot.SpringApplication.run;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@SpringBootApplication(scanBasePackages = "m4gshm.benchmark")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class SpringMvcApplication {
    private final Storage<TaskEntity, String> storage;
    private final Status OK = new Status(true);

    public static void main(String[] args) {
        run(SpringMvcApplication.class, args);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Task> get(
            @RequestAttribute(name = JFR_HTTP_REQUEST_EVENT, required = false) HttpEvent event,
            @PathVariable(value = "id") String id
    ) {
        var start = System.nanoTime();
        try {
            var task = storage.get(id);
            return task == null ? notFound().build() : ok(task);
        } finally {
            if (event != null) event.setControllerDuration(System.nanoTime() - start);
        }
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Collection<TaskEntity> list(
            @RequestAttribute(name = JFR_HTTP_REQUEST_EVENT, required = false) HttpEvent event
    ) {
        var start = System.nanoTime();
        try {
            return storage.getAll();
        } finally {
            if (event != null) event.setControllerDuration(System.nanoTime() - start);
        }
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public Status create(
            @RequestAttribute(name = JFR_HTTP_REQUEST_EVENT, required = false) HttpEvent event,
            @RequestBody TaskEntity task
    ) {
        var start = System.nanoTime();
        try {
            storage.store(initId(task));
            return OK;
        } finally {
            if (event != null) event.setControllerDuration(System.nanoTime() - start);
        }
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Status update(
            @RequestAttribute(name = JFR_HTTP_REQUEST_EVENT, required = false) HttpEvent event,
            @PathVariable("id") String id, @RequestBody TaskEntity task
    ) {
        var start = System.nanoTime();
        try {
            storage.store(task.withId(id));
            return OK;
        } finally {
            if (event != null) event.setControllerDuration(System.nanoTime() - start);
        }
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Status delete(
            @RequestAttribute(name = JFR_HTTP_REQUEST_EVENT, required = false) HttpEvent event,
            @PathVariable("id") String id
    ) {
        var start = System.nanoTime();
        try {
            if (storage.delete(id)) {
                return OK;
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } finally {
            if (event != null) event.setControllerDuration(System.nanoTime() - start);
        }
    }

    public record Status(boolean success) {
    }
}
