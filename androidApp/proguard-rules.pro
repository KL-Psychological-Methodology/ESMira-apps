## General Code and Kotlin Stuff

-keepattributes *Annotation*, InnerClasses, Signature, LineNumberTable, Exceptions

# Don't obfuscate ESMira code (should keep stack traces in error reports readable)
-keepnames class at.jodlidev.esmira.** { *; }

# Keep generated Kotlin metadata in Java classes necessary for reflection
-keep class kotlin.Metadata { *; }

# Keep enum classes

-keepclasseswithmembers enum * {
    public static**[] values();
    public static** valueOf(java.lang.String);
}

# Keep mappings of enums in when expressions
-keepclasseswithmembers class **$WhenMappings {
    <fields>;
}

# Keep (Kotlin) companion objects
-keepclassmembers class * {
    public static ** Companion;
}

## Parcelable

# Used in WelcomeScreenActivity and as navigation arguments
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

## QR Code Scanner

# Class referenced directly in Android Manifest
-keep class com.journeyapps.barcodescanner.CaptureActivity { *; }

## Dontwarns

-dontwarn okhttp3.**
-dontwarn io.noties.markwon.**

