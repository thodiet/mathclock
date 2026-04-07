package com.mathclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathclock.ui.theme.MathClockTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MathClockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        DigitalClock()
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalClock(modifier: Modifier = Modifier) {
    var currentDate by remember { mutableStateOf(Date()) }

    // Update the time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentDate = Date()
            delay(1000L)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatTime(currentDate),
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = timeInWords(currentDate),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

private fun formatTime(date: Date): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(date)
}

private fun timeInWords(date: Date): String {
    val cal = Calendar.getInstance().apply { time = date }
    val hour = cal.get(Calendar.HOUR) // 0-11
    val displayHour = if (hour == 0) 12 else hour
    val nextHour = if (displayHour == 12) 1 else displayHour + 1
    val minute = cal.get(Calendar.MINUTE)

    return when {
        minute == 0 -> "Es ist $displayHour Uhr"
        minute == 1 -> "Es ist eine Minute nach $displayHour"
        minute <= 30 -> "Es ist $minute Minuten nach $displayHour"
        minute == 59 -> "Es ist eine Minute vor $nextHour"
        else -> "Es ist ${60 - minute} Minuten vor $nextHour"
    }
}

@Preview(showBackground = true)
@Composable
fun DigitalClockPreview() {
    MathClockTheme {
        DigitalClock()
    }
}