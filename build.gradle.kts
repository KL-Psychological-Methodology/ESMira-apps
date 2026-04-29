buildscript {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
	dependencies {
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
		classpath("com.android.tools.build:gradle:9.2.0")
		classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.2.10")
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