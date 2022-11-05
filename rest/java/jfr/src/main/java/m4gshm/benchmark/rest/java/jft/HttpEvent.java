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
public class HttpEvent extends ScopeBasedEvent {

    public static HttpEvent create(Object method, Object path) {
        return BaseEvent.create(getName(method, path), HttpEvent::new);
    }

    public static HttpEvent start(Object method, Object path) {
        return BaseEvent.start(getName(method, path), HttpEvent::new);
    }

    private static String getName(Object method, Object path) {
        return method + ": " + path;
    }

}
