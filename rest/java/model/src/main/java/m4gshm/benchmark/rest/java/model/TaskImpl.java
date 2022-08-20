package m4gshm.benchmark.rest.java.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.OffsetDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.List.copyOf;


@Builder
@JsonInclude(NON_NULL)
public record TaskImpl(
        @Getter @With String id, @Getter String text, @Getter List<String> tags, @Getter OffsetDateTime deadline
) implements Task<TaskImpl, OffsetDateTime> {

    public TaskImpl(String id, String text, List<String> tags, OffsetDateTime deadline) {
        this.id = id;
        this.text = text;
        this.tags = tags != null ? copyOf(tags) : null;
        this.deadline = deadline;
    }

}
