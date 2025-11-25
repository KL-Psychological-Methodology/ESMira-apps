val composeVersion = "1.7.5"
val m3Version = "1.3.1"
val markwonVersion = "4.6.2"
val accompanistVersion = "0.28.0"

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
}

repositories {
    gradlePluginPortal() //for debug-db & barcodescanner:zxing
    maven {
        setUrl("https://jitpack.io") // for MPAndroidChart
    }
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

android {
    compileSdk = 35
    defaultConfig {
        applicationId = "at.jodlidev.esmira"
        minSdk = 21
        targetSdk = 35
        versionCode = 217
        versionName = "2.17.7"

        multiDexEnabled = true //project became too big. See: https://stackoverflow.com/a/59308589
    
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
    namespace = "at.jodlidev.esmira"
}

dependencies {
    implementation(project(":sharedCode"))
    implementation("com.google.android.material:material:1.12.0")
//    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    
    implementation("androidx.compose.material3:material3:$m3Version")
    implementation("androidx.compose.ui:ui:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion") // previews, ...
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion") // previews, ...
    implementation("androidx.compose.foundation:foundation:$composeVersion") // Border, Background, Box, Image, Scroll, shapes, animations
//    implementation("androidx.compose.material:material:$composeVersion") // Material Design
    implementation("androidx.compose.material:material-icons-core:$composeVersion") // Material design icons
    implementation("androidx.compose.material:material-icons-extended:$composeVersion") // Material design icons
    implementation("androidx.compose.runtime:runtime:$composeVersion") // Integration with observables
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion") // Integration with observables
    implementation("androidx.compose.foundation:foundation-layout:$composeVersion")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0") //ConstraintLayout (different versioning)
    
    //Custom transitions in compose are not implemented yet, so we need the experimental library:
    //https://issuetracker.google.com/issues/172112072
    //https://issuetracker.google.com/issues/197140101
    //implementation("com.google.accompanist:accompanist-navigation-animation:$accompanistVersion")
    implementation("androidx.navigation:navigation-compose:2.8.4")
//    implementation("androidx.navigation:navigation-compose:2.5.2") // Navigation
    
    implementation("androidx.multidex:multidex:2.0.1") //project became too big. Needed for SdK<21 See: https://stackoverflow.com/a/59308589)
    
    //debug db and sharedPreferences with browser (http://localhost:8080)
    //to access from outside, command for windows: C:\Users\[USERNAME]\AppData\Local\Android\sdk\platform-tools\adb forward tcp:8080 tcp:8080
    debugImplementation("com.github.amitshekhariitbhu.Android-Debug-Database:debug-db:1.0.7") // https://github.com/amitshekhariitbhu/Android-Debug-Database
    
    implementation("androidx.work:work-runtime:2.9.0") //WorkManager
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") //qr scanner
    
    implementation("androidx.preference:preference:1.2.1") //PreferenceManager
//    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") //https://github.com/PhilJay/MPAndroidChart
    //Unfortunately the original MPAndroidChart is dead. Using an updated fork instead:
    implementation("com.github.AppDevNext:AndroidChart:3.1.0.31")
    
    //HtmlView:
    implementation("io.noties.markwon:core:$markwonVersion")
    implementation("io.noties.markwon:html:$markwonVersion")
    implementation("io.noties.markwon:image:$markwonVersion")
    implementation("io.noties.markwon:linkify:$markwonVersion")
    implementation("io.noties.markwon:ext-strikethrough:$markwonVersion")
}
