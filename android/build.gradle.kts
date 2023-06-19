plugins {
    id("org.jetbrains.compose") version "1.3.0"
    id("com.android.application")
    kotlin("android")
}

val ktmidi_version = "0.5.0"

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.6.0")
    implementation("dev.atsushieno:ktmidi-android:$ktmidi_version")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "dev.atsushieno.kmmk"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}