package fibonacci

import (
	"fmt"
	"reflect"
	"runtime"
	"testing"

	"github.com/stretchr/testify/assert"
)

func Test_Fibonacci(t *testing.T) {

	type testData struct {
		input  uint8
		output uint64
	}

	funcs := []func(uint8) uint64{Fibonacci, Fibonacci2, Fibonacci3, FibonacciRecursive}
	tests := []testData{{0, 0}, {1, 1}, {2, 1}, {22, 17711}, {40, 102334155}}
	for _, f := range funcs {
		for _, test := range tests {
			funcInfo := runtime.FuncForPC(reflect.ValueOf(f).Pointer())
			funcName := funcInfo.Name()
			t.Run(fmt.Sprintf("%v(%d) -> %d", funcName, test.input, test.output), func(t *testing.T) {
				r := f(test.input)
				assert.Equal(t, test.output, r)
			})
		}
	}
}
