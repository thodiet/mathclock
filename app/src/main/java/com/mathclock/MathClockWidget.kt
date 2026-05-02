package com.mathclock

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import android.content.res.Configuration

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
        const val INITIAL_GRANULARITY = 15
        const val DEFAULT_LANGUAGE = "de"
        val TransparencyKey = floatPreferencesKey("transparency")
        val GranularityKey = intPreferencesKey("granularity")
        val LanguageKey = stringPreferencesKey("language")
        const val ACTION_UPDATE = "com.mathclock.ACTION_WIDGET_UPDATE"

        /**
         * Generic helper to update widget preferences.
         */
        private suspend fun <T> updateWidgetPreference(context: Context, key: Preferences.Key<T>, value: T) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(MathClockWidget::class.java)

            glanceIds.forEach { glanceId ->
                try {
                    updateAppWidgetState(
                        context,
                        PreferencesGlanceStateDefinition,
                        glanceId
                    ) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[key] = value
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MathClockWidget", "Failed to update ${key.name} for $glanceId", e)
                }
            }

            MathClockWidget().updateAll(context)
        }

        /**
         * Updates the transparency for all widgets and triggers an immediate redraw.
         */
        suspend fun updateTransparency(context: Context, transparency: Float) {
            Log.d("MathClockWidget", "updateTransparency called with: $transparency")
            updateWidgetPreference(context, TransparencyKey, transparency)
        }

        /**
         * Updates the granularity for all widgets and triggers an immediate redraw.
         */
        suspend fun updateGranularity(context: Context, granularity: Int) {
            Log.d("MathClockWidget", "updateGranularity called with: $granularity")
            updateWidgetPreference(context, GranularityKey, granularity)
        }

        /**
         * Updates the language for all widgets and triggers an immediate redraw.
         */
        suspend fun updateLanguage(context: Context, language: String) {
            Log.d("MathClockWidget", "updateLanguage called with: $language")
            updateWidgetPreference(context, LanguageKey, language)
        }

        /**
         * Creates a context with a specific locale.
         */
        @SuppressLint("AppBundleLocaleChanges")
        fun getLocalizedContext(context: Context, language: String): Context {
            val locale = Locale.forLanguageTag(language)
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("MathClockWidget", "provideGlance started for $id")
        provideContent {
            val glancePrefs = currentState<Preferences>()
            val transparency = glancePrefs[TransparencyKey] ?: INITIAL_TRANSPARENCY
            val granularity = glancePrefs[GranularityKey] ?: INITIAL_GRANULARITY
            val language = glancePrefs[LanguageKey] ?: DEFAULT_LANGUAGE

            Log.d("MathClockWidget", "Rendering with transparency: $transparency, granularity: $granularity, lang: $language")

            GlanceTheme {
                WidgetContent(context, transparency, granularity, language)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, transparency: Float, granularity: Int, language: String) {
        val now = Date()
        val size = LocalSize.current
        val localizedContext = getLocalizedContext(context, language)
        val wordTime = timeInWords(localizedContext, now, granularity)
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
            size.width < 150.dp -> 10.sp
            size.width < 250.dp -> 14.sp
            size.width < 350.dp -> 18.sp
            else -> 22.sp
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

                Spacer(modifier = GlanceModifier.height(6.dp))

                Text(
                    text = wordTime,
                    style = TextStyle(
                        fontSize = calculatedFontSize,
                        color = GlanceTheme.colors.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
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

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextMinute,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Log.e(
                "MathClockWidget",
                "Exact alarm permission not granted, falling back to non-exact alarm",
                e
            )
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                nextMinute,
                pendingIntent
            )
        }
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
