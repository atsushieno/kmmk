plugins {
    id("org.jetbrains.compose") version "1.0.0-alpha1-rc4"
    id("com.android.application")
    kotlin("android")
}

val ktmidi_version = "0.3.8"
val mugene_version = "0.2.8"

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.3.1")
    implementation("dev.atsushieno:ktmidi-android:$ktmidi_version")
    implementation("com.arkivanov.decompose:decompose:0.3.1")
    implementation("com.arkivanov.decompose:extensions-compose-jetpack:0.3.1")
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "dev.atsushieno.kmmk"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}