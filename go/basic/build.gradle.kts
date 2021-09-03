plugins {
    id("com.github.blindpirate.gogradle") version "0.11.4"
}

golang {
    packagePath = "."
}

tasks.create("goBenchmarks", com.github.blindpirate.gogradle.Go::class.java) {
    go("test -bench=. ./...")
}
