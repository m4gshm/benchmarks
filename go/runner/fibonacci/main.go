package main

import "benchmarks/basic/fibonacci"
import "time"
import "fmt"

var sum = uint64(0)

func main() {

	warmupCount := 1_000_000_000
	runCount := 1_000_000_000

	start := time.Now()
	for i := 0; i < warmupCount; i++ {
		sum += fibonacci.Fibonacci3(92)
	}
	finish := time.Since(start)
	fmt.Println("Warmup", finish.Milliseconds(), "ms")

	sum = 0
	start = time.Now()
	for i := 0; i < runCount; i++ {
		sum += fibonacci.Fibonacci3(92)
	}
	finish = time.Since(start)
	fmt.Println(finish.Milliseconds(), "ms")
}
