plugins {
    id("org.jetbrains.compose") version "0.4.0-build180"
    id("com.android.application")
    kotlin("android")
}

group = "dev.atsushieno"
version = "0.1.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    google()
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.3.0-alpha06")
    implementation("dev.atsushieno:ktmidi-android:0.2.7")
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