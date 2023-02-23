rootProject.name = "benchmarks"
pluginManagement {
    repositories {
        mavenLocal() {
            mavenContent {
//                snapshotsOnly()
            }
        }
        maven("https://repo.spring.io/release")
        maven("https://repo.spring.io/milestone")
        mavenCentral()
        gradlePluginPortal()
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
include(":rest:java:storage:model")
include(":rest:java:storage:model-jpa")
include(":rest:java:storage:jdbc")
include(":rest:java:storage:spring-data")
include(":rest:java:storage:querydsl-sql")
include(":rest:java:storage:querydsl-sql-jdbc")
include(":rest:java:storage:querydsl-sql-r2dbc")
include(":rest:java:storage:spring-data-reactive")
include(":rest:java:jfr")
include(":rest:java:mvc")
include(":rest:java:webflux")
include(":rest:java:webflux-native")
include(":rest:java:quarkus")
include(":rest:java:quarkus-reactive")
include(":rest:kotlin:ktor")
include(":rest:kotlin:storage")
//include(":rest:kotlin:storage-panache-reactive")
include(":rest:stress_tests")