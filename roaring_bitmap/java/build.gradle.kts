plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")

    implementation("org.roaringbitmap:RoaringBitmap:0.9.23")
    jmh("org.roaringbitmap:RoaringBitmap:0.9.23")
    testImplementation("junit:junit:4.13.2")
}

jmh {
    profilers.add("gc")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets.jmh {
    resources {
        srcDir("$rootDir/resources-roaring-bitmap")
    }
}
