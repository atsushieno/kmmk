buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("com.android.tools.build:gradle:7.0.3")
    }
}

subprojects {
    group = "dev.atsushieno"
    version = "0.2"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
        google()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}