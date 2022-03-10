tasks.create("goBenchmarks", Exec::class.java) {
    group = "benchmark"
    commandLine("go", "test", "-bench", ".", "-benchtime", "2s", "-benchmem")
    doFirst {
        mkdir(buildDir)
        standardOutput  = file("${buildDir}/results.txt").outputStream()
    }
}
