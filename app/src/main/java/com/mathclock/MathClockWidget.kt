package com.mathclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.DpSize
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.appwidget.updateAll
import androidx.glance.appwidget.SizeMode
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import androidx.glance.layout.Spacer
import androidx.glance.LocalSize
import androidx.glance.layout.height
import java.util.Date
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Box
import androidx.glance.GlanceTheme
import androidx.glance.layout.fillMaxWidth
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.GlanceAppWidgetManager

class MathClockWidget : GlanceAppWidget() {

        override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(100.dp, 50.dp),
            DpSize(150.dp, 50.dp),
            DpSize(250.dp, 50.dp),
            DpSize(350.dp, 50.dp)
        )
    )
    override val stateDefinition = PreferencesGlanceStateDefinition

    companion object {
        const val INITIAL_TRANSPARENCY = 40f
        val TransparencyKey = floatPreferencesKey("transparency")
        const val ACTION_UPDATE = "com.mathclock.ACTION_WIDGET_UPDATE"

        /**
         * Updates the transparency for all widgets and triggers an immediate redraw.
         */
        suspend fun updateTransparency(context: Context, transparency: Float) {
            Log.d("MathClockWidget", "updateTransparency called with: $transparency")

            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(MathClockWidget::class.java)

            Log.d("MathClockWidget", "Found ${glanceIds.size} widgets to update")

            glanceIds.forEach { glanceId ->
                try {
                    updateAppWidgetState(
                        context,
                        PreferencesGlanceStateDefinition,
                        glanceId
                    ) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[TransparencyKey] = transparency
                        }
                    }
                    Log.d("MathClockWidget", "State updated for $glanceId")
                } catch (e: Exception) {
                    Log.e("MathClockWidget", "Failed to update state for $glanceId", e)
                }
            }

            // Immediate update of all widgets
            MathClockWidget().updateAll(context)
            Log.d("MathClockWidget", "updateAll(context) executed")
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("MathClockWidget", "provideGlance started for $id")
        provideContent {
            val glancePrefs = currentState<Preferences>()
            val transparency = glancePrefs[TransparencyKey] ?: INITIAL_TRANSPARENCY

            Log.d("MathClockWidget", "Rendering with transparency: $transparency")

            GlanceTheme {
                WidgetContent(context, transparency)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, transparency: Float) {
        val now = Date()
        val size = LocalSize.current
        val wordTime = timeInWords(now)
        Log.d(
            "MathClockWidget",
            "WidgetContent rendering at $now (Size: ${size.width}x${size.height}) with text: $wordTime"
        )

        // Use transparency from state
        val alpha = (100 - transparency) / 100f
        val widgetBackgroundColor = ColorProvider(
            day = Color.White.copy(alpha = alpha),
            night = Color.Black.copy(alpha = alpha)
        )

        // Dynamic font size based on widget width (size.width is in dp)
        val calculatedFontSize = when {
            size.width < 150.dp -> 8.sp
            size.width < 250.dp -> 12.sp
            size.width < 350.dp -> 16.sp
            else -> 20.sp
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(widgetBackgroundColor)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                // System-managed TextClock (Uses ?android:attr/textColorPrimary in XML)
                AndroidRemoteViews(
                    remoteViews = RemoteViews(context.packageName, R.layout.widget_clock_layout),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                Text(
                    text = wordTime,
                    style = TextStyle(
                        fontSize = calculatedFontSize,
                        color = GlanceTheme.colors.onSurface
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

class MathClockWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MathClockWidget()

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MathClockWidget", "onReceive: ${intent.action}")

        when (intent.action) {
            MathClockWidget.ACTION_UPDATE,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_USER_PRESENT -> {
                val pendingResult = goAsync()
                MainScope().launch {
                    try {
                        Log.d("MathClockWidget", "Updating widget for ${intent.action}")
                        glanceAppWidget.updateAll(context)
                    } catch (e: Exception) {
                        Log.e("MathClockWidget", "Failed to update widget", e)
                    } finally {
                        pendingResult?.finish()
                    }
                }
                scheduleUpdate(context)
            }

            "android.appwidget.action.APPWIDGET_UPDATE" -> {
                super.onReceive(context, intent)
                scheduleUpdate(context)
            }

            else -> {
                super.onReceive(context, intent)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleUpdate(context)
    }

    private fun scheduleUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MathClockWidgetReceiver::class.java).apply {
            action = MathClockWidget.ACTION_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Target the exact start of the next minute
        val now = System.currentTimeMillis()
        val nextMinute = (now / 60000 + 1) * 60000

        Log.d("MathClockWidget", "Scheduling exact update for the next minute: $nextMinute")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextMinute,
            pendingIntent
        )
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdate(context)
    }

    private fun cancelUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MathClockWidgetReceiver::class.java).apply {
            action = MathClockWidget.ACTION_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
