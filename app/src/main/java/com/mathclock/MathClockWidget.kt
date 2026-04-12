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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import java.util.Date
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.GlanceTheme
import androidx.glance.layout.fillMaxWidth

class MathClockWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val now = Date()
        Log.d("MathClockWidget", "provideGlance started at $now")
        provideContent {
            GlanceTheme {
                WidgetContent(context, now)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, now: Date) {
        val wordTime = timeInWords(now)
        Log.d("MathClockWidget", "WidgetContent rendering at $now with text: $wordTime")

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
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
                        .height(30.dp)
                )
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                Text(
                    text = timeInWords(now),
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = GlanceTheme.colors.onSurface
                    )
                )
            }
        }
    }
}

class MathClockWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MathClockWidget()

    companion object {
        private const val ACTION_UPDATE = "com.mathclock.ACTION_WIDGET_UPDATE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MathClockWidget", "onReceive: ${intent.action}")
        
        when (intent.action) {
            ACTION_UPDATE -> {
                val pendingResult = goAsync()
                MainScope().launch {
                    try {
                        Log.d("MathClockWidget", "Manually triggering updateAll for ACTION_UPDATE")
                        glanceAppWidget.updateAll(context)
                    } catch (e: Exception) {
                        Log.e("MathClockWidget", "Failed to update widget", e)
                    } finally {
                        pendingResult?.finish()
                    }
                }
                scheduleUpdate(context)
            }
            Intent.ACTION_TIME_TICK, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                val pendingResult = goAsync()
                MainScope().launch {
                    try {
                        glanceAppWidget.updateAll(context)
                    } catch (e: Exception) {
                        Log.e("MathClockWidget", "Failed to update widget on time event", e)
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
            action = ACTION_UPDATE
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
            action = ACTION_UPDATE
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
