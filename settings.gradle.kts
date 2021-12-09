rootProject.name = "benchmarks"

include(":java-basic")
project(":java-basic").projectDir = file("java/basic")
include(":java-json")
project(":java-json").projectDir = file("java/json")
include(":java-protobuf")
project(":java-protobuf").projectDir = file("java/protobuf")
include(":java-roaring-bitmap")
project(":java-roaring-bitmap").projectDir = file("java/roaring-bitmap")
include(":go")
project(":go").projectDir = file("go")
