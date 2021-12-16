import org.jetbrains.compose.compose

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.0.0"
}

val ktmidi_version = "0.3.16"

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
                implementation("dev.atsushieno:ktmidi:$ktmidi_version")
                implementation("dev.atsushieno:mugene:0.2.26")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
                implementation("com.arkivanov.decompose:decompose:0.4.0")
                implementation("io.ktor:ktor-io:1.6.1")
                implementation("dev.atsushieno:compose-mpp:0.1.3")
            }
        }
        val commonTest by getting
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.4.0")
                api("androidx.core:core-ktx:1.7.0")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val desktopMain by getting
        val desktopTest by getting
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 31
    }
}
