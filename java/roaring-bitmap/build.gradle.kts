plugins {
    java
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    implementation("org.projectlombok:lombok:1.18.20")

    implementation("org.roaringbitmap:RoaringBitmap:0.9.23")
    jmh("org.roaringbitmap:RoaringBitmap:0.9.23")
}

tasks.create("roaringBitmapBenchmarks") {
    group = "benchmark"
    dependsOn("jmh")
}

sourceSets.jmh {
    resources {
        srcDir("$rootDir/resources")
    }
}

jmh {
    forceGC.set(true)
//    profilers.add("jfr")
    verbosity.set("extra")
}