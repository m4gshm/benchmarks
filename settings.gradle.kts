rootProject.name = "benchmarks"
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.spring.io/release")
        mavenCentral()
//        maven("https://repo.spring.io/libs-release-local")
    }
}

include(":map:go")
include(":map:java")
include(":json:go")
include(":json:java")
include(":protobuf:go")
include(":protobuf:java")
include(":roaring_bitmap:go")
include(":roaring_bitmap:java")
include(":rest:go")
include(":rest:java:jmh")
include(":rest:java:model")
include(":rest:java:jfr")
include(":rest:java:mvc")
include(":rest:java:webflux")
include(":rest:java:webflux-native")
include(":rest:java:quarkus")
//include(":rest:kotlin:ktor")
//include(":rest:kotlin:ktor-graalvm-native")
include(":rest:kotlin:storage")
//include(":rest:kotlin:storage-multiplatform")
include(":rest:stress_tests")