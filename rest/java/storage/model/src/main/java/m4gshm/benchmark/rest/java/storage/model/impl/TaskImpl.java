package m4gshm.benchmark.rest.java.storage.model.impl;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import m4gshm.benchmark.rest.java.storage.model.IdAware;
import m4gshm.benchmark.rest.java.storage.model.Task;
import m4gshm.benchmark.rest.java.storage.model.WithId;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder(toBuilder = true)
public record TaskImpl(
        @Getter @With String id,
        @Getter String text,
        @Getter LocalDateTime deadline,
        @Getter Set<String> tags
) implements Task<LocalDateTime>, IdAware<String>, WithId<TaskImpl, String> {

    public static TaskImpl initId(TaskImpl task) {
        var id = task.getId();
        return id == null ? task.toBuilder().id(UUID.randomUUID().toString()).build() : task;
    }

    @Override
    public TaskImpl withId(String id) {
        return this.toBuilder().id(id).build();
    }
}
