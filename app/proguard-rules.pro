# Jetpack Compose Glance Rules
-keep class androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class com.mathclock.MathClockWidgetReceiver { *; }
-keep class com.mathclock.MathClockWidget { *; }

# App Startup
-keep class androidx.startup.InitializationProvider { *; }

# UI Components & Resources
-keep class android.widget.RemoteViews { *; }
-keep class android.widget.TextClock { *; }
-keep class com.mathclock.R$layout { *; }
-keep class com.mathclock.R$id { *; }

# System requirements for Glance
-keep public class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keep public class * extends androidx.glance.appwidget.GlanceAppWidget

# Compose Runtime
-keep class androidx.compose.runtime.ParcelableSnapshotState* { *; }
-keep @interface androidx.compose.runtime.Composable

# Lifecycle
-keep class androidx.lifecycle.**Initializer { *; }

# Resource Keeping
-keepclassmembers class **.R$* {
    public static <fields>;
}
