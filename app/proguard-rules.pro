# noinspection ShrinkerUnresolvedReference
# Jetpack Compose Glance Rules
-keep class androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class androidx.glance.state.GlanceStateDefinition { *; }
-keep class com.mathclock.MathClockWidgetReceiver { *; }
-keep class com.mathclock.MathClockWidget { *; }

# WorkManager (Specific rules to prevent reflection crashes)
# These may show "Unresolved class name" in the IDE if transitive, but are required for R8
-dontwarn androidx.work.**
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.InputMerger { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-keep class androidx.work.OverwritingInputMerger { *; }

# Room (Used internally by WorkManager)
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase

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
-keep class androidx.compose.runtime.ParcelableSnapshotState* { *; }
-keep @interface androidx.compose.runtime.Composable

# Lifecycle
-keep class androidx.lifecycle.ProcessLifecycleOwner { *; }
-keep class androidx.lifecycle.**Initializer { *; }

# Prevent resource shrinking from removing widget layouts
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Coroutines
-keep class kotlinx.coroutines.android.** { *; }
