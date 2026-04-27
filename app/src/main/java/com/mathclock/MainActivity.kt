package com.mathclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import com.mathclock.ui.theme.MathClockTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class Screen {
    Clock, Info
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MathClockTheme {
                var currentScreen by remember { mutableStateOf(Screen.Clock) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    when (currentScreen) {
                                        Screen.Clock -> "Widget Settings"
                                        Screen.Info -> "Info"
                                    }
                                )
                            },
                            navigationIcon = {
                                if (currentScreen != Screen.Clock) {
                                    IconButton(onClick = { currentScreen = Screen.Clock }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Zurück"
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (currentScreen == Screen.Clock) {
                                    IconButton(onClick = { currentScreen = Screen.Info }) {
                                        Icon(Icons.Default.Info, contentDescription = "Info")
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        when (currentScreen) {
                            Screen.Clock -> DigitalClock()
                            Screen.Info -> InfoScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalClock() {
    val current = LocalContext.current
    var currentDate by remember { mutableStateOf(Date()) }

    // Initial value, will be updated by LaunchedEffect
    var transparency by remember {
        mutableFloatStateOf(MathClockWidget.INITIAL_TRANSPARENCY)
    }

    // Load initial value from Glance state
    LaunchedEffect(Unit) {
        val manager = GlanceAppWidgetManager(current)
        val glanceIds = manager.getGlanceIds(MathClockWidget::class.java)
        if (glanceIds.isNotEmpty()) {
            val state = MathClockWidget().getAppWidgetState<Preferences>(
                current,
                glanceIds.first()
            )
            transparency =
                state[MathClockWidget.TransparencyKey] ?: MathClockWidget.INITIAL_TRANSPARENCY
        }
    }

    // Update the time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentDate = Date()
            delay(1000L)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hintergrund-Transparenz: ${transparency.roundToInt()}%",
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = transparency,
            onValueChange = {
                transparency = it
            },
            onValueChangeFinished = {
                MainScope().launch {
                    MathClockWidget.updateTransparency(current.applicationContext, transparency)
                }
            },
            valueRange = 0f..100f
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatTime(currentDate),
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = timeInWords(currentDate),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatTime(date: Date): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(date)
}

@Composable
fun InfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Willkommen zur Math Clock",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Jesus Christus spricht:\n" +
                    "Ich bin der Weg und die Wahrheit und das Leben; " +
                    "niemand kommt zum Vater denn durch mich.\n" +
                    "Joh. 14,6\n\n" +
                    "David betet:\n" +
                    "Meine Zeit steht in deinen Händen.\n" +
                    "Ps. 31,16\n\n" +
                    "Vor allem im Süden Deutschlands kennt man die Ausdrücke Viertel sieben " +
                    "und Drei Viertel sieben und meint damit 6:15 bzw. 6:45.\n" +
                    "Für alle, die Freude daran haben oder sich damit vertraut machen möchten, " +
                    "habe ich diese App entwickelt.\n" +
                    "Mittels eines Widgets kann man sich die Anzeige " +
                    "auch auf den Startbildschirm legen.\n" +
                    "Wegen Android-Limitierungen bei Widgets kann es sein, dass die Zeitangabe initial nachgeht. " +
                    "Das sollte sich nach 1-2 Minuten stabilisieren.\n\n" +
                    "Alle Ehre dem Herrn Jesus Christus!",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DigitalClockPreview() {
    MathClockTheme {
        DigitalClock()
    }
}