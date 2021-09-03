tasks.create("goBenchmarks", Exec::class.java) {
    commandLine("go", "test", "-bench", ".", "./...")
}
