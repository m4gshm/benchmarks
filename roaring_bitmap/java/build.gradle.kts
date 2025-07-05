plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("org.projectlombok:lombok:1.18.38")

    implementation("org.roaringbitmap:RoaringBitmap:0.9.23")
    jmh("org.roaringbitmap:RoaringBitmap:0.9.23")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
}

jmh {
    profilers.add("gc")
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

sourceSets.jmh {
    resources {
        srcDir("$rootDir/resources-roaring-bitmap")
    }
}
