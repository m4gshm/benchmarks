package m4gshm.benchmark.rest.quarkus.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Builder
@JsonInclude(NON_NULL)
public record Status(String id, boolean success) {
    public static final Status OK = new Status(null, true);
}
