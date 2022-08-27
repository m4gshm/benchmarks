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
@Description("Storage Event")
@ToString(callSuper = true)
@RequiredArgsConstructor(access = PRIVATE)
public class StorageEvent extends ScopeBasedEvent {

    public static StorageEvent start(String name) {
        return BaseEvent.start(name, StorageEvent::new);
    }

    public static StorageEvent create(String name) {
        return BaseEvent.create(name, StorageEvent::new);
    }

    @Override
    public void close() {
        finish();
    }

}
