import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.3.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

val ktmidi_version = "0.4.0"

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
            // LAMESPEC: cannot specify the actual app version due to this bug https://github.com/JetBrains/compose-jb/issues/2360
            packageVersion = "1.0.0"
        }
    }
}
