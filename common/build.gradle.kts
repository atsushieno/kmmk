import org.jetbrains.compose.compose

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.4.0"
}

val ktmidi_version = "0.5.0"
val mugene_version = "0.4.0"

kotlin {
    jvmToolchain(17)
    android()
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
                implementation("dev.atsushieno:ktmidi:$ktmidi_version")
                implementation("dev.atsushieno:mugene:$mugene_version")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("io.ktor:ktor-io:2.3.0")
                implementation("dev.atsushieno:compose-mpp:0.1.3")
            }
        }
        val commonTest by getting
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.9.0")
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
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
    }
}
