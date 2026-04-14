# Jetpack Compose Glance Rules
-keep class androidx.glance.appwidget.** { *; }
-keep class com.mathclock.MathClockWidgetReceiver { *; }
-keep class com.mathclock.MathClockWidget { *; }
-keep class androidx.glance.** { *; }

# WorkManager (essential for Glance background updates)
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker { public <init>(...); }
-keep class * extends androidx.work.InputMerger { public <init>(...); }

# Room (used by WorkManager)
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.work.impl.background.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.work.impl.**

# App Startup
-keep class androidx.startup.** { *; }

# UI Components & Resources
-keep class android.widget.RemoteViews { *; }
-keep class android.widget.TextClock { *; }
-keep class com.mathclock.R$layout { *; }
-keep class com.mathclock.R$id { *; }

# System requirements
-keep public class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keep public class * extends androidx.glance.appwidget.GlanceAppWidget

# Compose & Lifecycle
-keep class androidx.compose.runtime.** { *; }
-keep @interface androidx.compose.runtime.Composable
-keep class androidx.lifecycle.ProcessLifecycleOwnerInitializer { *; }

# Resource Keeping
-keepclassmembers class **.R$* {
    public static <fields>;
}
