package fibonacci

func Fibonacci(number uint8) uint64 {
	var (
		current uint64
		prev    uint64
	)

	for i := uint8(0); i <= number; i++ {
		switch i {
		case 0:
			current = 0
		case 1:
			current = 1
		case 2:
			current = 1
			prev = 1

		default:
			var tmp = current
			current = current + prev
			prev = tmp
		}
	}
	return current
}

func Fibonacci2(number uint8) uint64 {
	var (
		current uint64
		prev    uint64
	)

	for i := uint8(0); i <= number; i++ {
		switch i {
		case 1:
			current = 1
		case 2:
			current = 1
			prev = 1

		default:
			if i != 0 {
				var tmp = current
				current = current + prev
				prev = tmp
			}

		}
	}
	return current
}

func Fibonacci3(number uint8) uint64 {
	switch number {
	case 0:
		return 0
	case 1, 2:
		return 1
	}
	var (
		current uint64 = 1
		prev    uint64 = 1
	)
	for i := uint8(3); i <= number; i++ {
		tmp := current
		current = current + prev
		prev = tmp
	}
	return current
}

func FibonacciRecursive(number uint8) uint64 {
	r, _ := _fibonacciRecursive(number)
	return r
}

func _fibonacciRecursive(number uint8) (uint64, uint64) {
	if number > uint8(1) {
		current, prev := _fibonacciRecursive(number - 1)

		nextPrev := current
		return current + prev, nextPrev

	}
	return uint64(number), 0
}

