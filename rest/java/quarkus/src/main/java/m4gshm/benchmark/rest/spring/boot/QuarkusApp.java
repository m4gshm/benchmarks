package m4gshm.benchmark.rest.spring.boot;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class QuarkusApp {
    public static void main(String... args) {
        Quarkus.run(args);
    }
}
