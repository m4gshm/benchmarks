tasks.create("goBenchmarks", Exec::class.java) {
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "./...")
}

tasks.create("jsonBenchmarks", Exec::class.java) {
    group = "benchmark"
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "./basic/json")
}

tasks.create("mapBenchmarks", Exec::class.java) {
    group = "benchmark"
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "./basic/map")
}

tasks.create("roaringBitmapBenchmarks", Exec::class.java) {
    group = "benchmark"
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "./roaring_bitmap")
}