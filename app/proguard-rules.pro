# Jetpack Compose Glance Rules
-keep class androidx.glance.appwidget.** { *; }
-keep class androidx.glance.state.** { *; }
-keep class com.mathclock.MathClockWidgetReceiver { *; }
-keep class com.mathclock.MathClockWidget { *; }

# WorkManager (Full protection for reflection-based instantiation)
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keep class * extends androidx.work.InputMerger

# Room (Used internally by WorkManager)
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# DataStore & Preferences
-keep class androidx.datastore.preferences.core.** { *; }
-keep class androidx.datastore.core.** { *; }

# App Startup
-keep class androidx.startup.InitializationProvider { *; }

# UI Components & Resources
-keep class android.widget.RemoteViews { *; }
-keep class android.widget.TextClock { *; }
-keep class com.mathclock.R$layout { *; }
-keep class com.mathclock.R$id { *; }

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }
-keep @interface androidx.compose.runtime.Composable

# Lifecycle
-keep class androidx.lifecycle.** { *; }

# Prevent resource shrinking from removing widget layouts
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Coroutines
-keep class kotlinx.coroutines.android.** { *; }
