buildscript {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
	dependencies {
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
		classpath("com.android.tools.build:gradle:8.11.2")
		classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.0.21")
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