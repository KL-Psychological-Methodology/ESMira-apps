val ktorVersion = "2.0.1" //for web requests (combined with JSON serialisation)
val serializationVersion = "1.3.2" //for JSON; https://github.com/Kotlin/kotlinx.serialization
val coroutinesVersion = "1.6.1" //for multiple threads (used for web-requests)

plugins {
	kotlin("multiplatform")
	id("com.android.library")
	kotlin("plugin.serialization") version "1.6.10"
}

allprojects {
	repositories {
		google()
		mavenCentral()
	}
}

kotlin {
	android()
	
	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64()
	).forEach {
		it.binaries.framework {
			baseName = "sharedCode"
		}
	}
	
	//ktor, coroutines and serialization are a tricky combination:
	//https://youtrack.jetbrains.com/issue/KTOR-915
	//https://medium.com/@kpgalligan/ktor-and-kotlin-native-fb5c06cb920a

//	val ktorVersion = "1.5.2" //for web requests (combined with JSON serialisation)
//	val serializationVersion = "1.1.0" //for JSON; https://github.com/Kotlin/kotlinx.serialization
//	val coroutinesVersion = "1.4.3" //for multiple threads (used for web-requests)
	sourceSets {
		
		
		val commonMain by getting {
			dependencies {
				implementation("io.ktor:ktor-client-core:$ktorVersion")
				implementation("io.ktor:ktor-client-serialization:$ktorVersion")
				implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
				implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
				implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
				
//				implementation("io.ktor:ktor-client-core:$ktorVersion")
//				implementation("io.ktor:ktor-client-json:$ktorVersion")
//				implementation("io.ktor:ktor-client-serialization:$ktorVersion")
//				implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
//
//				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion-native-mt") {
//					version {
//						strictly("$coroutinesVersion-native-mt")
//					}
//				}
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}
		val androidMain by getting {
			dependencies {
				implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
//				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
//				implementation("io.ktor:ktor-client-android:$ktorVersion")
			}
		}
		val androidTest by getting {
			//this is for unit tests - used by commonTest
//			dependencies {
//				implementation(kotlin("test-junit"))
//				implementation("junit:junit:4.13.2")
//			}
		}
		val androidAndroidTest by getting {
			//this is for instrumented testing (sqlite on android using an emulator)
			//see: https://touchlab.co/understanding-and-configuring-your-kmm-test-suite/
			dependencies {
				implementation("androidx.test.ext:junit:1.1.3")
				implementation("androidx.test.ext:junit-ktx:1.1.3")
				implementation("androidx.test.espresso:espresso-core:3.4.0")
			}
		}
		val iosX64Main by getting
		val iosArm64Main by getting
		val iosSimulatorArm64Main by getting
		val iosMain by creating {
			dependencies {
				implementation("io.ktor:ktor-client-darwin:$ktorVersion")
//				implementation("io.ktor:ktor-client-ios:$ktorVersion")
			}
			dependsOn(commonMain)
			iosX64Main.dependsOn(this)
			iosArm64Main.dependsOn(this)
			iosSimulatorArm64Main.dependsOn(this)
		}
		val iosX64Test by getting
		val iosArm64Test by getting
		val iosSimulatorArm64Test by getting
		val iosTest by creating {
			dependsOn(commonTest)
			iosX64Test.dependsOn(this)
			iosArm64Test.dependsOn(this)
			iosSimulatorArm64Test.dependsOn(this)
		}
	}
}

android {
	compileSdk = 31
	sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
	defaultConfig {
		minSdk = 16
		targetSdk = 31
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
}