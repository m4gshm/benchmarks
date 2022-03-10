plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    implementation("org.projectlombok:lombok:1.18.22")

    implementation("org.roaringbitmap:RoaringBitmap:0.9.23")
    jmh("org.roaringbitmap:RoaringBitmap:0.9.23")
    testImplementation("junit:junit:4.13.2")
}

jmh {
    profilers.add("gc")
}

sourceSets.jmh {
    resources {
        srcDir("$rootDir/resources-roaring-bitmap")
    }
}
