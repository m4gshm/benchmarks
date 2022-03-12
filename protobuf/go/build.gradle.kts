tasks.create("goBenchmarks", Exec::class.java) {
    group = "benchmark"
    commandLine("go", "mod", "tidy")
    commandLine("go", "test", "-bench", ".", "-benchtime", "5s", "-benchmem")
    doFirst {
        mkdir(buildDir)
        standardOutput  = file("${buildDir}/results.txt").outputStream()
    }
}
