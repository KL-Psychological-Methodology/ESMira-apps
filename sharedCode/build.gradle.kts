import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization") version "1.4.10"
}

kotlin {
    android()

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iosTarget("ios") {
        binaries {
            framework {
                baseName = "sharedCode"
            }
        }
    }

    //ktor, coroutines and serialization are a tricky combination:
    //https://youtrack.jetbrains.com/issue/KTOR-915
    //https://medium.com/@kpgalligan/ktor-and-kotlin-native-fb5c06cb920a

//    val ktorVersion = "1.4.1" //for web requests (combined with JSON serialisation)
//    val serializationVersion = "1.0.1" //for JSON; https://github.com/Kotlin/kotlinx.serialization
//    val coroutinesVersion = "1.4.1" //for multiple threads (used for web-requests)
    val ktorVersion = "1.5.2" //for web requests (combined with JSON serialisation)
    val serializationVersion = "1.1.0" //for JSON; https://github.com/Kotlin/kotlinx.serialization
    val coroutinesVersion = "1.4.3" //for multiple threads (used for web-requests)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-json:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion-native-mt") {
                    version {
                        strictly("$coroutinesVersion-native-mt")
                    }
                }
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

//                implementation("io.ktor:ktor-client-android:$ktorVersion")
//                implementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")

                implementation("io.ktor:ktor-client-android:$ktorVersion")
            }
        }
        val androidTest by getting {
            //this is for unit tests - used by commonTest
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
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
        val iosMain by getting {
            dependencies {
                //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
//                implementation("io.ktor:ktor-client-serialization:$ktorVersion")

                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosTest by getting
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(16)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    
    //androidMain is not shown as a Module. We can add it manually:
    //https://youtrack.jetbrains.com/issue/KT-46472
    //https://youtrack.jetbrains.com/issue/KT-44789
    //https://discuss.kotlinlang.org/t/androidmain-and-android-tests-folder-not-recognised-as-module/19203
//    sourceSets {
//        getByName("main") {
//            java.srcDirs(file("src/android${name.capitalize()}/kotlin"))
////            java.srcDirs(file("../iosApp/ESMira/ESMira"))
//        }
//    }
}

val packForXcode by tasks.creating(Sync::class) {
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val framework = kotlin.targets.getByName<KotlinNativeTarget>("ios").binaries.getFramework(mode)
    val targetDir = File(buildDir, "xcode-frameworks")

    group = "build"
    dependsOn(framework.linkTask)
    inputs.property("mode", mode)

    from({ framework.outputDirectory })
    into(targetDir)
}

tasks.getByName("build").dependsOn(packForXcode)