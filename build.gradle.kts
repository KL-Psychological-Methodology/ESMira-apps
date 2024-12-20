buildscript {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
	dependencies {
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
		classpath("com.android.tools.build:gradle:8.7.1")
	}
}

allprojects {
	repositories {
		google()
		mavenCentral()
	}
}

tasks.register("clean", Delete::class) {
	delete(getLayout().buildDirectory)
}