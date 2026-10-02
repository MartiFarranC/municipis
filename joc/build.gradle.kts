plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "cat.descobreix"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(kotlin("test-junit"))
}

tasks.test {
    // Els tests llegeixen la configuració i els assets reals del repositori.
    systemProperty("arrel.repositori", rootDir.parentFile.absolutePath)
}
