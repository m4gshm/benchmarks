package m4gshm.benchmark.fibonacci;


import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FibonacciTest {

    final Fibonacci fibonacci = new Fibonacci(true);

    @Test
    public void fibonacciRecursive() {
        runTest(fibonacci::fibonacciRecursive);
    }

    @Test
    public void fibonacciRecursive2() {
        runTest(fibonacci::fibonacciRecursive2);
    }

    @Test
    public void fibonacci() {
        runTest(fibonacci::fibonacci);
    }

    @Test
    public void fibonacci2() {
        runTest(fibonacci::fibonacci2);
    }

    @Test
    public void fibonacci3() {
        runTest(fibonacci::fibonacci3);
    }

    private void runTest(Function<Integer, Long> calc) {
        testData().forEach(data -> assertEquals(data.expected, calc.apply(data.input)));
    }


    private Stream<Data> testData() {
        return of(data(0, 0), data(1, 1), data(22, 17711), data(40, 102334155));
    }

    private Data data(int input, long ouput) {
        return new Data(input, ouput);
    }

    record Data(int input, long expected) {
    }
}
