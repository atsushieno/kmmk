plugins {
    id("org.jetbrains.compose") version "0.5.0-build224"
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
    implementation("androidx.activity:activity-compose:1.3.0-beta01")
    implementation("dev.atsushieno:ktmidi-android:0.2.8.5")
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "dev.atsushieno.kmmk"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}