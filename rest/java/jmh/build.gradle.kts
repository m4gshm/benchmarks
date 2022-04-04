import me.champeau.jmh.JMHTask

plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
//    jmh("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    jmh(project(":rest:java:storage"))
    jmh(project(":rest:java:webflux"))
    jmh("com.playtika.reactivefeign:feign-reactor-webclient:3.2.1")
    jmh("com.playtika.reactivefeign:feign-reactor-spring-configuration:3.2.1")
}

jmh {
//    jvmArgsAppend.addAll("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
    forceGC.set(true)
    profilers.add("gc")
}
