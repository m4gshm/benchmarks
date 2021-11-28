rootProject.name = "benchmarks"

include(":java-basic")
project(":java-basic").projectDir = file("java/basic")
include(":java-json")
project(":java-json").projectDir = file("java/json")
include(":java-grpc")
project(":java-grpc").projectDir = file("java/grpc")
include(":java-roaring-bitmap")
project(":java-roaring-bitmap").projectDir = file("java/roaring-bitmap")
include(":go")
project(":go").projectDir = file("go")
