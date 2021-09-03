rootProject.name = "benchmarks"

include(":java-basic")
project(":java-basic").projectDir = file("java/basic")
include(":java-json")
project(":java-json").projectDir = file("java/json")
include(":go-basic")
project(":go-basic").projectDir = file("go/basic")
