# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Firebase classes
-keep class com.google.firebase.** { *; }

# Keep data classes for Firestore
-keepclassmembers class com.speedreader.trainer.domain.model.** {
    *;
}

# Keep Retrofit interfaces
-keepattributes Signature
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

