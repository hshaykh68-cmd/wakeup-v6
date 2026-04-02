# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number table, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Room entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Vico Charting Library
-keep class com.patrykandpatrick.vico.** { *; }
-keepclassmembers class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# Media3 ExoPlayer
-keep class androidx.media3.** { *; }
-keepclassmembers class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# ML Kit Barcode Scanning
-keep class com.google.mlkit.vision.barcode.** { *; }
-keepclassmembers class com.google.mlkit.vision.barcode.** { *; }
-dontwarn com.google.mlkit.vision.barcode.**
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.android.gms.vision.**

# Keep CameraX (used with ML Kit)
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Keep Sleep Sound Service
-keep class com.wakeup.app.data.service.SleepSoundService { *; }

# Keep BatteryOptimizationHelper
-keep class com.wakeup.app.core.util.BatteryOptimizationHelper { *; }
-keep class android.os.PowerManager { *; }

# Keep MediaPlayer
-keep class android.media.MediaPlayer { *; }
-keep class android.media.AudioManager { *; }
-keep class android.media.AudioAttributes { *; }
-keep class android.media.AudioFocusRequest { *; }
