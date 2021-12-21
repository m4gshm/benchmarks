package m4gshm.benchmark.fibonacci;

import java.util.function.IntToLongFunction;
import java.util.function.LongSupplier;

import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Runner {
    private static final Fibonacci fibonacci = new Fibonacci(false);
    public static volatile long r;
    private static volatile int value = 92;

    public static void main(String[] args) {
        value = value;
        var warmupCount = 1_000_000_000;
        var runCount = 1_000_000_000;

        var clock = (LongSupplier) System::currentTimeMillis;
        var timeUnit = MILLISECONDS;

        var fibo = (IntToLongFunction) fibonacci::fibonacci3;

        var start = clock.getAsLong();
        long sum = 0;
        for (int i = 0; i < warmupCount; i++) {
            sum += i + fibo.applyAsLong(value);
        }
        var finish = clock.getAsLong();
        out.println("Warmup " + timeUnit.toMillis(finish - start) + "ms");

        sum = 0;
        start = clock.getAsLong();
        for (int i = 0; i < runCount; i++) {
            sum += i + fibo.applyAsLong(92);
        }
        finish = clock.getAsLong();
        out.println(timeUnit.toMillis(finish - start) + "ms");
        r = sum;
        out.println(sum);
    }
}
