package json

import (
	"fmt"
	"testing"
)

func Benchmark_MapOfMapByKey(b *testing.B) {

	m1 := make(map[string]map[string]string)
	key := "one"

	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		_, ok := m1[key]
		if !ok {
			m1[key] = make(map[string]string)
		}
		m1[key]["two"] = "three"
	}

	b.StopTimer()

	fmt.Printf("1 %v\n", b.N)
}

func Benchmark_Benchmark_MapOfMapByRef(b *testing.B) {

	m1 := make(map[string]map[string]string)
	key := "one"

	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		m2, ok := m1[key]
		if !ok {
			m2 = make(map[string]string)
			m1[key] = m2
		}
		m2["two"] = "three"
	}

	b.StopTimer()

	fmt.Printf("2 %v\n", b.N)
}

func Benchmark_Benchmark_MapOfMapEveryUpdateByKey(b *testing.B) {
	m1 := make(map[string]map[string]string)
	key := "one"
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		m2, ok := m1[key]
		if !ok {
			m2 = make(map[string]string)
		}
		m2["two"] = "three"
		m1[key] = m2
	}

	b.StopTimer()

	fmt.Printf("3 %v\n", b.N)
}
