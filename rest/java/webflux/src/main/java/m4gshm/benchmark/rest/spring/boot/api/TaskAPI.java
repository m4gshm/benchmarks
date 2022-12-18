package m4gshm.benchmark.rest.spring.boot.api;

import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.spring.boot.storage.r2dbc.model.TaskEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface TaskAPI {

    String ROOT_PATH_TASK = "/task";

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    Task get(@PathVariable(value = "id") String id);

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    List<Task> list();

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    Status create(@RequestBody TaskEntity task);

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    Status update(@PathVariable("id") String id, @RequestBody TaskEntity task);

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    Status delete(@PathVariable("id") String id);

    record Status(boolean success) {
    }
}
