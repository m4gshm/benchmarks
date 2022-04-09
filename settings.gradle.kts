rootProject.name = "benchmarks"
pluginManagement {
    repositories {
//        maven("https://repo.spring.io/libs-release-local")
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spring.io/release")

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
include(":rest:java:storage")
include(":rest:java:mvc")
include(":rest:java:webflux")
include(":rest:java:webflux-native")
include(":rest:stress_tests")