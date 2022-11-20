package m4gshm.benchmark.rest.java.jfr;

import jdk.jfr.Enabled;
import jdk.jfr.Registered;
import jdk.jfr.StackTrace;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@RequiredArgsConstructor(access = PROTECTED)
@Registered(false)
@Enabled(false)
@StackTrace(false)
public abstract class ScopeBasedEvent extends BaseEvent implements AutoCloseable {
    @Override
    public void close() {
        finish();
    }
}
