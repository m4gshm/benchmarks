plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
    id("com.google.protobuf") version "0.8.18"
}


repositories {
    maven("https://plugins.gradle.org/m2/")
}

val protobufVersion = "3.19.1"
val grpcVersion = "1.42.1"

dependencies {

    compileOnly("me.champeau.jmh:jmh-gradle-plugin:0.6.6")

//    compileOnly("com.google.protobuf:protobuf-gradle-plugin:0.8.18")
//    compileOnly("com.google.protobuf:com.google.protobuf.gradle.plugin:0.8.16")


    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    listOf(
        "io.grpc:grpc-protobuf:$grpcVersion",
        "io.grpc:grpc-stub:$grpcVersion",
        "io.grpc:protoc-gen-grpc-java:$grpcVersion",

        "com.google.protobuf:protobuf-java:$protobufVersion",
        "com.google.protobuf:protobuf-javalite:$protobufVersion",
        "com.google.protobuf:protobuf-java-util:$protobufVersion",
    ).forEach { dependency ->
        listOf("implementation", "jmh").forEach { configName ->
            add(configName, dependency)
        }
    }

    testImplementation("junit:junit:4.13.2")
}

jmh {
    profilers.addAll("gc", "stack")

//    profilers.addAll( "xperfasm")
//    profilers.addAll( "jfr")
//    val os = DefaultNativePlatform.getCurrentOperatingSystem()
//    if (os.isWindows) profilers.addAll("xperfasm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    main {
        java {
            srcDir("${project.buildDir}/generated/source/proto/main/java")
        }
        resources {
            srcDir("$rootDir/resources-protobuf")
        }
    }
}

protobuf {
    protobuf.apply {
        protoc(closureOf<com.google.protobuf.gradle.ExecutableLocator> {
            artifact = "com.google.protobuf:protoc:$protobufVersion"
        })
//        plugins(closureOf<NamedDomainObjectContainer<com.google.protobuf.gradle.ExecutableLocator>> {
//            create("grpc") {
//                artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
//            }
//        })
        generateProtoTasks(closureOf<com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection> {
            all().forEach { task ->
                task.builtins {
                    create("go")
                    "java" {}
                }
            }
        })
    }
}
