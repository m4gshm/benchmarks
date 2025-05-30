package m4gshm.benchmark.rest.spring.boot;

import lombok.RequiredArgsConstructor;
import m4gshm.benchmark.rest.java.storage.Storage;
import m4gshm.benchmark.rest.java.storage.model.Task;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Collection;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@Controller
@RequiredArgsConstructor
public abstract class TaskGraphQlController<T extends Task> {

    private final Storage<T, String> storage;

    @QueryMapping
    public ResponseEntity<T> taskById(@Argument String id) {
        var task = storage.get(id);
        return task == null ? notFound().build() : ok(task);
    }

    @QueryMapping
    public Collection<T> tasks() {
        return storage.getAll();
    }

    @MutationMapping
    public T createTask(@Argument T input) {
        return storage.store(input);
    }

}
