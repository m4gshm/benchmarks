package m4gshm.benchmark.fibonacci;

public class Fibonacci {

    private final CheckOverflow checker;

    public Fibonacci(boolean checkOverflow) {
        checker = checkOverflow ? (current, number) -> {
            if (current < 0) throw new IllegalArgumentException("fibonacci overflow " + current + ", for " + number);
        } : (current, number) -> {
        };
    }

    private void checkOverflow(long current, int number) {
        checker.checkOverflow(current, number);
    }

    private void _fibonacciRecursive(int number, Accumulator result) {
        if (number > 1) {
            _fibonacciRecursive(number - 1, result);
            long nextPrev = result.current;
            var current = result.current + result.prev;
            checkOverflow(current, number);
            result.current = current;
            result.prev = nextPrev;
        } else {
            result.current = number;
        }
    }

    private Result _fibonacciRecursive(int number) {
        if (number > 1) {
            var prev = _fibonacciRecursive(number - 1);
            var current = prev.current + prev.prev;
            checkOverflow(current, number);
            return new Result(current, prev.current);
        }
        return new Result(number, 0);
    }

    long fibonacci2(int number) {
        long current = 0;
        long prev = 0;

        for (int i = 0; i <= number; i++) {
            switch (i) {
                case 1 -> current = 1;
                case 2 -> {
                    current = 1;
                    prev = 1;
                }
                default -> {
                    if (i != 0) {
                        var tmp = current;
                        current = current + prev;
                        prev = tmp;
                        checkOverflow(current, i);
                    }
                }
            }
        }
        return current;
    }

    long fibonacci(int number) {
        long current = 0;
        long prev = 0;

        for (int i = 0; i <= number; i++) {
            switch (i) {
                case 0 -> current = 0;
                case 1 -> current = 1;
                case 2 -> {
                    current = 1;
                    prev = 1;
                }
                default -> {
                    var tmp = current;
                    current = current + prev;
                    prev = tmp;
                    checkOverflow(current, i);
                }
            }
        }
        return current;
    }

    long fibonacci3(int number) {
        switch (number) {
            case 0:
                return 0;
            case 1:
            case 2:
                return 1;
            default:
                //first number ==3
                long current = 1;
                long prev = 1;
                for (int i = 3; i <= number; i++) {
                    var tmp = current;
                    current = current + prev;
                    prev = tmp;
                    checkOverflow(current, i);
                }
                return current;
        }
    }

    /**
     * @param number range from  0 to 92 inclusive
     */
    public long fibonacciRecursive(int number) {
        return _fibonacciRecursive(number).current;
    }

    public long fibonacciRecursive2(int number) {
        var accumulator = new Accumulator();
        _fibonacciRecursive(number, accumulator);
        return accumulator.current;
    }

    private interface CheckOverflow {
        void checkOverflow(long current, int number);
    }

    private record Result(long current, long prev) {
    }

    private static class Accumulator {
        long current, prev;
    }
}
