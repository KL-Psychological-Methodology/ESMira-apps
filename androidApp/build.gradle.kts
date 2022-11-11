val composeVersion = "1.2.1"

plugins {
    id("com.android.application")
    kotlin("android")
}

repositories {
    gradlePluginPortal() //for debug-db & barcodescanner:zxing
    maven {
        setUrl("https://jitpack.io") // for MPAndroidChart
    }
    mavenCentral()
}

android {
    compileSdk = 32
    defaultConfig {
        applicationId = "at.jodlidev.esmira"
        minSdk = 21
        targetSdk = 32
        versionCode = 129
        versionName = "2.7.5"
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.1"
    }
}

dependencies {
    implementation(project(":sharedCode"))
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    //jetpack compose:
    //the plan is, that we will slowly migrate to jetpack and then move to material 3
    implementation("androidx.compose.ui:ui:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion") // previews, ...
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion") // previews, ...
    implementation("androidx.compose.foundation:foundation:$composeVersion") // Border, Background, Box, Image, Scroll, shapes, animations
    implementation("androidx.compose.material:material:$composeVersion") // Material Design
    implementation("androidx.compose.material:material-icons-core:$composeVersion") // Material design icons
    implementation("androidx.compose.material:material-icons-extended:$composeVersion") // Material design icons
    implementation("androidx.compose.runtime:runtime:$composeVersion") // Integration with observables
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion") // Integration with observables
    implementation("androidx.compose.foundation:foundation-layout:$composeVersion")
    implementation("androidx.navigation:navigation-compose:2.5.2") // Navigation
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1") //ConstraintLayout (different versioning)
    
    implementation("androidx.multidex:multidex:2.0.1") //project became too big. Needed for SdK<21 See: https://stackoverflow.com/a/59308589)
    
    //debug db and sharedPreferences with browser (http://localhost:8080)
    //to access from outside, command for windows: C:\Users\[USERNAME]\AppData\Local\Android\sdk\platform-tools\adb forward tcp:8080 tcp:8080
    debugImplementation("com.amitshekhar.android:debug-db:1.0.4") // https://github.com/amitshekhariitbhu/Android-Debug-Database
    
    implementation("androidx.work:work-runtime:2.7.1") //WorkManager
    implementation("me.dm7.barcodescanner:zxing:1.9.13") //qr scanner
    implementation("androidx.preference:preference:1.2.0") //PreferenceFragment
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") //https://github.com/PhilJay/MPAndroidChart
    
    implementation("com.github.NightWhistler:HtmlSpanner:0.4") //https://github.com/NightWhistler/HtmlSpanner
    
    implementation("com.otaliastudios:cameraview:2.7.2") //https://github.com/natario1/CameraView
}