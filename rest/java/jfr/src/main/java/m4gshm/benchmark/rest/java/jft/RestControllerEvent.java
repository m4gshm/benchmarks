package m4gshm.benchmark.rest.java.jft;

import jdk.jfr.Description;
import jdk.jfr.Enabled;
import jdk.jfr.Registered;
import jdk.jfr.StackTrace;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static lombok.AccessLevel.PRIVATE;


@Enabled(true)
@Registered(true)
@StackTrace(false)
@Description("REST Controller Event")
@ToString(callSuper = true)
@RequiredArgsConstructor(access = PRIVATE)
public class RestControllerEvent extends ScopeBasedEvent {

    public static RestControllerEvent start(String name) {
        return BaseEvent.start(name, RestControllerEvent::new);
    }

    @Override
    public void close() {
        finish();
    }

}
