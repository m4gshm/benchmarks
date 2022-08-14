package m4gshm.benchmark.rest.java.jft;

import jdk.jfr.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;
import static jdk.jfr.Timespan.MILLISECONDS;
import static lombok.AccessLevel.PROTECTED;


@Getter
@Setter
@ToString
@RequiredArgsConstructor(access = PROTECTED)
//@Registered(false)
@Category(RestControllerEvent.APPLICATION)
public class ApplicationEvent extends Event {
    public static final String APPLICATION = "Application";
    @Label("name")
    protected String name;
    @Label("Start Time")
    @Timestamp()
    protected long recordingStart;
    @Label("Recording Duration")
    @Timespan(MILLISECONDS)
    protected long recordingDuration;


    protected static <T extends ApplicationEvent> T start(String name, Supplier<T> constructor) {
        T event = create(name, constructor);
        event.start();
        return event;
    }

    protected static <T extends ApplicationEvent> T create(String name, Supplier<T> constructor) {
        var event = constructor.get();
        event.name = name;
        return event;
    }

    public void start() {
        recordingStart = currentTimeMillis();
        begin();
    }

    public void finish() {
        recordingDuration = currentTimeMillis() - recordingStart;
        end();
        if (shouldCommit()) {
            commit();
        }
    }
}
