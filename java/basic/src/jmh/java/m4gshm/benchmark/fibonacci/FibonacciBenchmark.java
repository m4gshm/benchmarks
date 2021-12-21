package m4gshm.benchmark.fibonacci;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Fork(value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
public class FibonacciBenchmark {

    final Fibonacci calcWithoutCheck = new Fibonacci(false);

    @Param({"92"})
    public int value;

    @Benchmark
    public void fibonacciRecursive(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacciRecursive(value));
    }

    @Benchmark
    public void fibonacciRecursive2(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacciRecursive2(value));
    }

    @Benchmark
    public void fibonacci(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacci(value));
    }

    @Benchmark
    public void fibonacci2(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacci2(value));
    }

    @Benchmark
    public void fibonacci3(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacci3(value));
    }

    @Benchmark
    public void fibonacci3Const(Blackhole blackhole) {
        blackhole.consume(calcWithoutCheck.fibonacci3(92));
    }

}
