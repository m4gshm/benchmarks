package m4gshm.benchmark.rest.java.jft;

import jdk.jfr.Description;
import jdk.jfr.Registered;
import jdk.jfr.StackTrace;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static lombok.AccessLevel.PRIVATE;


@Registered
@StackTrace(false)
@Description("REST Controller Event")
@ToString(callSuper = true)
@RequiredArgsConstructor(access = PRIVATE)
public class RestControllerEvent extends ApplicationEvent implements AutoCloseable {

    public static RestControllerEvent start(String name) {
        return ApplicationEvent.start(name, RestControllerEvent::new);
    }

    @Override
    public void close() {
        finish();
    }

}
