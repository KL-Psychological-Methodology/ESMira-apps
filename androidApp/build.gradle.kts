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
    compileSdk = 31
    defaultConfig {
        applicationId = "at.jodlidev.esmira"
        minSdk = 16
        targetSdk = 31
        versionCode = 110
        versionName = "2.6.0.2"
        multiDexEnabled = true //project became too big. See: https://stackoverflow.com/a/59308589
    
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":sharedCode"))
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    
//    implementation("com.google.android.material:material:1.3.0-alpha02") //1.3.0 has this problem: https://github.com/material-components/material-components-android/issues/1955
//    implementation("androidx.appcompat:appcompat:1.2.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
//    implementation("androidx.core:core-ktx:1.3.2")
    
    implementation("androidx.multidex:multidex:2.0.1") //project became too big. Needed for SdK<21 See: https://stackoverflow.com/a/59308589)
    
    //debug db and sharedPreferences with browser (http://localhost:8080)
    //command for windows: C:\Users\[USERNAME]\AppData\Local\Android\sdk\platform-tools\adb forward tcp:8080 tcp:8080
    debugImplementation("com.amitshekhar.android:debug-db:1.0.4") // https://github.com/amitshekhariitbhu/Android-Debug-Database
    
    implementation("androidx.work:work-runtime:2.7.1") //WorkManager
    implementation("me.dm7.barcodescanner:zxing:1.9.13") //qr scanner
    implementation("androidx.preference:preference:1.2.0") //PreferenceFragment
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") //https://github.com/PhilJay/MPAndroidChart
    
    implementation("com.github.NightWhistler:HtmlSpanner:0.4") //https://github.com/NightWhistler/HtmlSpanner
    
    implementation("com.otaliastudios:cameraview:2.7.2") //https://github.com/natario1/CameraView
}