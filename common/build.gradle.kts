plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.4.3"
}

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
                implementation(libs.ktmidi)
                implementation(libs.mugene)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.io)
                implementation(libs.compose.mpp)
            }
        }
        val commonTest by getting
        val androidMain by getting {
            dependencies {
                api(libs.appcompat)
                api(libs.core.ktx)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
        val desktopMain by getting
        val desktopTest by getting
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
    }
    namespace = "dev.atsushieno.kmmk"
}
