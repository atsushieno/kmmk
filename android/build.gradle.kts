plugins {
    id("org.jetbrains.compose") version "1.4.3"
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(project(":common"))
    implementation(libs.activity.compose)
    implementation(libs.ktmidi)
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
    namespace = "dev.atsushieno.kmmk.android"
}