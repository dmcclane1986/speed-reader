# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Firebase classes
-keep class com.google.firebase.** { *; }

# Keep Retrofit classes
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Keep data classes for Gson
-keep class com.speedreader.trainer.domain.model.** { *; }
-keep class com.speedreader.trainer.data.remote.** { *; }

