plugins {
    kotlin("jvm") version "2.3.10"
    java
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.0")
}

sourceSets.main {
    java.srcDir("src")
    resources.srcDir("resources")
}

sourceSets.test {
    java.srcDir("test")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    test {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
    }
}
