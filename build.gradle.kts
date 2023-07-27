buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.gradle)
    }
}

subprojects {
    group = "dev.atsushieno"
    version = "0.4"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
        google()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}