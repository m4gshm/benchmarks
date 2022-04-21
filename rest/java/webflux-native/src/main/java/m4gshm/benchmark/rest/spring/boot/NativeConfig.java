package m4gshm.benchmark.rest.spring.boot;

import m4gshm.benchmark.rest.java.model.Task;
import org.springframework.context.annotation.Configuration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;

@Configuration
@NativeHint(types = @TypeHint(types = {Task.class, Task[].class}))
public class NativeConfig {
}
