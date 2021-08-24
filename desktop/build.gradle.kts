import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.0.0-alpha1-rc4"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

val ktmidi_version = "0.3.8"
val mugene_version = "0.2.8"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
                implementation("dev.atsushieno:ktmidi-jvm:$ktmidi_version")
                implementation("dev.atsushieno:ktmidi-jvm-desktop:$ktmidi_version")
                implementation("com.arkivanov.decompose:decompose:0.3.1")
                implementation("com.arkivanov.decompose:extensions-compose-jetbrains:0.3.1")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "dev.atsushieno.kmmk.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "kmmk"
            packageVersion = "1.0.0"
        }
    }
}
