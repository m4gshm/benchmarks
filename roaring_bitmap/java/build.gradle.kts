plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")

    implementation("org.roaringbitmap:RoaringBitmap:0.9.23")
    jmh("org.roaringbitmap:RoaringBitmap:0.9.23")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}

jmh {
    profilers.add("gc")
}



sourceSets.jmh {
    resources {
        srcDir("$rootDir/resources-roaring-bitmap")
    }
}
