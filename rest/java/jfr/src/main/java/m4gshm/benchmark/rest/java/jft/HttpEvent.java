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
public class HttpEvent extends BaseEvent {

    public static HttpEvent create(String name) {
        return BaseEvent.create(name, HttpEvent::new);
    }

    public static HttpEvent start(String name) {
        return BaseEvent.start(name, HttpEvent::new);
    }
}
