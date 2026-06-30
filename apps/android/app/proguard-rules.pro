# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room database classes
-keep class com.maximeze.browser.data.db.** { *; }

# Keep serialization classes
-keepattributes *Annotation*
-dontwarn kotlinx.serialization.**
-keep class kotlinx.serialization.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
