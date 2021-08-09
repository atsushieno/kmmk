buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
        classpath("com.android.tools.build:gradle:4.2.2")
    }
}

allprojects {
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