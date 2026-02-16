pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "ESMira"
include(":androidApp")
include(":sharedCode")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}