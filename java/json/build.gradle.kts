plugins {
    java
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    implementation("org.projectlombok:lombok:1.18.20")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.5")
    jmh("com.fasterxml.jackson.core:jackson-databind:2.12.5")
}

sourceSets.jmh {
    resources {
        srcDir("$rootDir/resources")
    }
}

jmh {
    forceGC.set(true)
//    profilers.add("pauses")
}
