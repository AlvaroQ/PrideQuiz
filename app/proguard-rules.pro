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

# Source file name obfuscation is configured at the bottom of this file
# (see -renamesourcefileattribute SourceFile)
# Add this global rule
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keepclassmembers class com.google.firebase.database.GenericTypeIndicator { *; }
-keep class com.google.firebase.database.GenericTypeIndicator { *; }
-keepattributes Signature

-keepattributes RuntimeVisibleAnnotations
-keep class * extends androidx.navigation.Navigator

# Domain models usados por Firebase (serializacion/deserializacion)
-keep class com.quiz.domain.Pride { *; }
-keep class com.quiz.domain.Name { *; }
-keep class com.quiz.domain.User { *; }
-keep class com.quiz.domain.XpLeaderboardEntry { *; }
-keep class com.quiz.domain.App { *; }
-keep class com.quiz.domain.UserProfile { *; }
-keep class com.quiz.domain.LevelInfo { *; }
-keep class com.quiz.domain.Achievement { *; }
-keep class com.quiz.domain.GameMode { *; }
-keep class com.quiz.domain.GameResult { *; }
-keep class com.quiz.domain.PlayerStatistics { *; }
-keep class com.quiz.domain.XpGainResult { *; }

# DTOs para Firebase deserialization
-keep class com.quiz.pride.datasource.dto.** { *; }

# Obfuscar nombres de archivo en stack traces (Crashlytics tiene el mapping)
-renamesourcefileattribute SourceFile

-dontwarn javax.annotation.**
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn org.junit.**
-dontwarn org.hamcrest.**

# Room (requerido por WorkManager que viene como dependencia transitiva de Firebase)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract <methods>;
}

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keep class androidx.work.impl.** { *; }

# Crashlitics
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

# Accesibilidad (Play Console pre-launch report)
# El escaner de accesibilidad identifica Views por su nombre de clase Java.
# Sin esta regla, R8 renombra AndroidComposeView -> "ef0" y el escaner reporta
# "tipo no compatible con servicios de accesibilidad" (falso positivo).
# -keepnames solo preserva el nombre de la clase; miembros se siguen ofuscando.
-keepnames class * extends android.view.View