import org.jetbrains.compose.compose

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "0.4.0-build180"
}

group = "dev.atsushieno"
version = "0.1.0"

val ktmidi_version = "0.2.4"
val mugene_version = "0.2.3"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    google()
}

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
                // FIXME: shouldn't there be nicer way to simply specify ktmidi API here instead of per-plat sections?
                implementation("dev.atsushieno:ktmidi-kotlinMultiplatform:$ktmidi_version")
                implementation("dev.atsushieno:mugene-kotlinMultiplatform:$mugene_version")
            }
        }
        val commonTest by getting
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.2.0")
                api("androidx.core:core-ktx:1.3.2")
                implementation("dev.atsushieno:ktmidi-android:$ktmidi_version")
                implementation("dev.atsushieno:mugene-android:$mugene_version")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation("dev.atsushieno:ktmidi-jvm:$ktmidi_version")
                implementation("dev.atsushieno:mugene-jvm:$mugene_version")
            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)
    }
}