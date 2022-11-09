package m4gshm.benchmark.rest.java.jft;

import jdk.jfr.*;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static java.lang.System.nanoTime;
import static jdk.jfr.Timespan.NANOSECONDS;
import static lombok.AccessLevel.PRIVATE;

@Enabled(true)
@Registered(true)
@StackTrace(false)
@Description("REST Controller Event")
@ToString(callSuper = true)
@RequiredArgsConstructor(access = PRIVATE)
public class HttpEvent extends ScopeBasedEvent {

    @Label("Preprocess Duration")
    @Timespan(NANOSECONDS)
    protected long preprocessDuration;

    @Label("Process Duration")
    @Timespan(NANOSECONDS)
    protected long processDuration;

    @Setter
    @Label("Controller Duration")
    @Timespan(NANOSECONDS)
    protected long controllerDuration;

    private long processStart;

    public static HttpEvent create(Object method, Object path) {
        return BaseEvent.create(getName(method, path), HttpEvent::new);
    }

    public static HttpEvent start(Object method, Object path) {
        return BaseEvent.start(getName(method, path), HttpEvent::new);
    }

    private static String getName(Object method, Object path) {
        return method + ": " + path;
    }

    public void finishPreprocess() {
        processStart = nanoTime();
        preprocessDuration = processStart - getRecordingStart();
    }

    public void finishProcess() {
        processDuration = nanoTime() - processStart;
    }
}
