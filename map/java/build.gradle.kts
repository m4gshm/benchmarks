import me.champeau.jmh.JMHTask

plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    jmh("com.fasterxml.jackson.core:jackson-databind:2.13.0")
}

jmh {
    forceGC.set(true)
    profilers.add("gc")
}
