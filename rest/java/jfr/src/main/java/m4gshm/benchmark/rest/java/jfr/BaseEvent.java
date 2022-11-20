package m4gshm.benchmark.rest.java.jfr;

import jdk.jfr.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Supplier;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;


//@Getter
//@Setter
@ToString
@RequiredArgsConstructor(access = PROTECTED)
@Registered(false)
@Enabled(false)
@StackTrace(false)
@Category(RestControllerEvent.APPLICATION)
public class BaseEvent extends Event {
    public static final String APPLICATION = "Application";
    @Label("name")
    protected String name;
    @Label("Recording Duration")
    @Timespan
    protected long recordingDuration;
    @Getter(PACKAGE)
    private long recordingStart;

    @Label("Thread Start")
    protected String threadStart;
    @Label("Thread Finish")
    protected String threadFinish;

    protected static <T extends BaseEvent> T start(String name, Supplier<T> constructor) {
        T event = create(name, constructor);
        event.start();
        return event;
    }

    protected static <T extends BaseEvent> T create(String name, Supplier<T> constructor) {
        var event = constructor.get();
        event.name = name;
        return event;
    }

    public void start() {
        begin();
        threadStart = Thread.currentThread().getName();
        recordingStart = System.nanoTime();
    }

    public void finish() {
        recordingDuration = System.nanoTime() - recordingStart;
        threadFinish = Thread.currentThread().getName();
        end();
        if (shouldCommit()) {
            commit();
        }
    }
}
