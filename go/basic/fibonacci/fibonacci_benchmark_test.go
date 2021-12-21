package fibonacci

import (
	"reflect"
	"runtime"
	"testing"
)

func Benchmark_Fibonacci(b *testing.B) {

	funcs := []func(uint8) uint64{Fibonacci, Fibonacci2, Fibonacci3, FibonacciRecursive}

	for _, f := range funcs {
		funcInfo := runtime.FuncForPC(reflect.ValueOf(f).Pointer())
		funcName := funcInfo.Name()
		b.Run(funcName, func(b *testing.B) {
			for i := 0; i < b.N; i++ {
				f(92)
			}
		})

	}
}
