rootProject.name = "benchmarks"

include(":java-basic")
project(":java-basic").projectDir = file("java/basic")
include(":go-basic")
project(":go-basic").projectDir = file("go/basic")
