tasks.create("goBenchmarks", Exec::class.java) {
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "-benchmem", "./...")
}

tasks.create("jsonBenchmarks", Exec::class.java) {
    group = "benchmark"
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "-benchmem", "./basic/json")
}

tasks.create("mapBenchmarks", Exec::class.java) {
    group = "benchmark"
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "-benchmem", "./basic/map")
}

tasks.create("roaringBitmapBenchmarks", Exec::class.java) {
    group = "benchmark"
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "-benchmem", "./roaring_bitmap")
}

tasks.create("protobufBenchmarks", Exec::class.java) {
    group = "benchmark"
    val pprofOut = "protobuf_cpu.out"
    commandLine(
        "go", "test", "-bench", ".", "-benchtime", "5s", "-benchmem", "-cpuprofile",
        pprofOut, "-run=^$", "-bench", "^Benchmark_deserializeProtoFromBin2", "./protobuf"
    )
    commandLine("go", "tool", "pprof", "-top", pprofOut)
}