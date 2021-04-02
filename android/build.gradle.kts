plugins {
    id("org.jetbrains.compose") version "0.4.0-build177"
    id("com.android.application")
    kotlin("android")
}

group = "dev.atsushieno"
version = "1.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    google()
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.3.0-alpha05")
    // FIXME: Should this be required here?
    implementation("dev.atsushieno:ktmidi-jvm:0.1.9")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "dev.atsushieno.kmmk"
        minSdkVersion(24)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}