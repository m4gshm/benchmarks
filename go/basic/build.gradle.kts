tasks.create("goBenchmarks", Exec::class.java) {
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "./...")
}

tasks.create("jsonBenchmarks", Exec::class.java) {
    group = "benchmark"
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "./json")
}

tasks.create("mapBenchmarks", Exec::class.java) {
    group = "benchmark"
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "./map")
}