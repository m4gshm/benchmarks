package m4gshm.benchmark.fibonacci;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Fork(value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
public class FibonacciBenchmark {

    final Fibonacci calcWithCheck = new Fibonacci(true);
    final Fibonacci calcWithoutCheck = new Fibonacci(false);

    @Benchmark
    public void fibonacciRecursive(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacciRecursive(92));
    }

    @Benchmark
    public void fibonacciRecursiveWithCheckOverflow(Blackhole blackhole) {
        blackhole.consume(calcWithCheck.fibonacciRecursive(92));
    }

    @Benchmark
    public void fibonacciRecursive2(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacciRecursive2(92));
    }

    @Benchmark
    public void fibonacciRecursive2WithCheckOverflow(Blackhole blackhole) {
        blackhole.consume(calcWithCheck.fibonacciRecursive2(92));
    }

    @Benchmark
    public void fibonacci(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacci(92));
    }

    @Benchmark
    public void fibonacciWithCheckOverflow(Blackhole blackhole) {
        blackhole.consume(calcWithCheck.fibonacci(92));
    }


    @Benchmark
    public void fibonacci2(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacci2(92));
    }

    @Benchmark
    public void fibonacci2WithCheckOverflow(Blackhole blackhole) {
        blackhole.consume(calcWithCheck.fibonacci2(92));
    }

    @Benchmark
    public void fibonacci3(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacci3(92));
    }

    @Benchmark
    public void fibonacci3WithCheckOverflow(Blackhole blackhole) {
        blackhole.consume(calcWithCheck.fibonacci3(92));
    }
}
