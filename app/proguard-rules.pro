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

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#Firebase
-keep class com.google.firebase.** { *; }
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class androidx.lifecycle.DefaultLifecycleObserver

#Crashlytics
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception

# ===== DataStore with Reflection & Generic Types =====
# Keep AppSettings data class and all its fields (used with reflection)
-keep class com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings {
    *;
}

# Keep all enum classes used in AppSettings
-keep enum com.mathias8dev.memoriesstoragexplorer.domain.enums.** { *; }

# Keep Kotlin reflection for KProperty1
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }

# Keep LocalAppSettingsDataSource and its generic methods
-keep class com.mathias8dev.memoriesstoragexplorer.data.datasource.local.datastore.LocalAppSettingsDataSource {
    public <methods>;
}

# Keep DataStore related classes
-keep class androidx.datastore.** { *; }
-keep interface androidx.datastore.** { *; }

# Keep serializers for DataStore
-keep class com.mathias8dev.memoriesstoragexplorer.data.datasource.local.datastore.serializers.** { *; }

# Keep generic type information for reflection
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# Keep Parcelize annotations for AppSettings
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public <fields>;
}

# Keep field names for reflection-based field access
-keepclassmembernames class com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings {
    <fields>;
}

# Koin dependency injection
-keep class org.koin.** { *; }
-keep class org.koin.core.annotation.** { *; }