plugins {
    id("org.jetbrains.compose") version "1.0.0"
    id("com.android.application")
    kotlin("android")
}

val ktmidi_version = "0.3.16"

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("dev.atsushieno:ktmidi-android:$ktmidi_version")
    implementation("com.arkivanov.decompose:decompose:0.4.0")
    implementation("com.arkivanov.decompose:extensions-compose-jetpack:0.4.0")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "dev.atsushieno.kmmk"
        minSdk = 24
        targetSdk = 31
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}