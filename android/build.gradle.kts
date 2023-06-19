plugins {
    id("org.jetbrains.compose") version "1.4.0"
    id("com.android.application")
    kotlin("android")
}

val ktmidi_version = "0.5.0"

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("dev.atsushieno:ktmidi:$ktmidi_version")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "dev.atsushieno.kmmk"
        minSdk = 24
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}