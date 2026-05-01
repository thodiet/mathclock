package com.mathclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import java.util.Calendar
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

                BackHandler(enabled = currentScreen == Screen.Info) {
                    currentScreen = Screen.Clock
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    when (currentScreen) {
                                        Screen.Clock -> stringResource(R.string.settings_title)
                                        Screen.Info -> stringResource(R.string.info_title)
                                    }
                                )
                            },
                            navigationIcon = {
                                if (currentScreen != Screen.Clock) {
                                    IconButton(onClick = { currentScreen = Screen.Clock }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.back_content_description)
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (currentScreen == Screen.Clock) {
                                    IconButton(onClick = { currentScreen = Screen.Info }) {
                                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.info_title))
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
    var granularity by remember {
        mutableIntStateOf(MathClockWidget.INITIAL_GRANULARITY)
    }

    // Load initial values from Glance state
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
            granularity =
                state[MathClockWidget.GranularityKey] ?: MathClockWidget.INITIAL_GRANULARITY
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
            text = stringResource(R.string.transparency_label, transparency.roundToInt()),
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.granularity_label),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        val radioOptions = listOf(15, 5)
        val radioLabels = listOf(
            stringResource(R.string.granularity_15),
            stringResource(R.string.granularity_5)
        )

        Row(
            Modifier
                .selectableGroup()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            radioOptions.forEachIndexed { index, option ->
                Row(
                    Modifier
                        .selectable(
                            selected = (option == granularity),
                            onClick = {
                                granularity = option
                                MainScope().launch {
                                    MathClockWidget.updateGranularity(
                                        current.applicationContext,
                                        granularity
                                    )
                                }
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (option == granularity),
                        onClick = null // null because of selectable modifier
                    )
                    Text(
                        text = radioLabels[index],
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

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
                text = timeInWords(current, currentDate, granularity),
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
    val context = LocalContext.current
    val infoText = buildAnnotatedString {
        append(stringResource(R.string.info_jesus_christ))
        withLink(
            LinkAnnotation.Url(
                url = "https://www.bibleserver.com/Ne%C3%9C/Johannes14%2C6",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) { append(stringResource(R.string.info_john_14_6)) }
        append(stringResource(R.string.info_david_pray))
        withLink(
            LinkAnnotation.Url(
                url = "https://www.bibleserver.com/LUT/Psalm31%2C16",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) { append(stringResource(R.string.info_psalm_31_16)) }
        append(stringResource(R.string.info_explanation))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = infoText,
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

@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    MathClockTheme {
        InfoScreen()
    }
}

@Preview(showBackground = true, name = "Show 15 minutes times", heightDp = 800)
@Composable
fun TimeInWordsTestPreview() {
    val locale = LocalConfiguration.current.locales[0]
    val sdf = SimpleDateFormat("HH:mm:ss", locale)

    // Test-Daten generieren
    val baseCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    val testZeiten = listOf(0, 1, 7, 8, 14, 15, 16, 22, 23, 29, 30, 31,
        37, 38, 44, 45, 46, 52, 53, 59).map { mins ->
        val cal = baseCal.clone() as Calendar
        cal.set(Calendar.MINUTE, mins)
        cal.time
    }

    MathClockTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "timeInWords, granularity = 15",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val splitIndex = (testZeiten.size + 1) / 2
            val firstHalf = testZeiten.take(splitIndex)
            val secondHalf = testZeiten.drop(splitIndex)

            Row(modifier = Modifier.fillMaxWidth()) {
                // left column
                Column(modifier = Modifier.weight(1f)) {
                    firstHalf.forEach { date ->
                        TimeTestItem(date, sdf)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // right column
                Column(modifier = Modifier.weight(1f)) {
                    secondHalf.forEach { date ->
                        TimeTestItem(date, sdf)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Show 5 minutes times", heightDp = 800)
@Composable
fun TimeInWords5_19TestPreview() {
    val locale = LocalConfiguration.current.locales[0]
    val sdf = SimpleDateFormat("HH:mm:ss", locale)

    // Test-Daten generieren
    val baseCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    val testZeiten = (0..19).map { mins ->
        val cal = baseCal.clone() as Calendar
        cal.set(Calendar.MINUTE, mins)
        cal.time
    }

    MathClockTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "timeInWords, granularity = 5, 0-19",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val splitIndex = (testZeiten.size + 1) / 2
            val firstHalf = testZeiten.take(splitIndex)
            val secondHalf = testZeiten.drop(splitIndex)

            Row(modifier = Modifier.fillMaxWidth()) {
                // left column
                Column(modifier = Modifier.weight(1f)) {
                    firstHalf.forEach { date ->
                        TimeTestItem(date, sdf, 5)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // right column
                Column(modifier = Modifier.weight(1f)) {
                    secondHalf.forEach { date ->
                        TimeTestItem(date, sdf, 5)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Show 5 minutes times", heightDp = 800)
@Composable
fun TimeInWords5_39TestPreview() {
    val locale = LocalConfiguration.current.locales[0]
    val sdf = SimpleDateFormat("HH:mm:ss", locale)

    // Test-Daten generieren
    val baseCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    val testZeiten = (20..39).map { mins ->
        val cal = baseCal.clone() as Calendar
        cal.set(Calendar.MINUTE, mins)
        cal.time
    }

    MathClockTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "timeInWords, granularity = 5, 20-39",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val splitIndex = (testZeiten.size + 1) / 2
            val firstHalf = testZeiten.take(splitIndex)
            val secondHalf = testZeiten.drop(splitIndex)

            Row(modifier = Modifier.fillMaxWidth()) {
                // left column
                Column(modifier = Modifier.weight(1f)) {
                    firstHalf.forEach { date ->
                        TimeTestItem(date, sdf, 5)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // right column
                Column(modifier = Modifier.weight(1f)) {
                    secondHalf.forEach { date ->
                        TimeTestItem(date, sdf, 5)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Show 5 minutes times", heightDp = 800)
@Composable
fun TimeInWords5_59TestPreview() {
    val locale = LocalConfiguration.current.locales[0]
    val sdf = SimpleDateFormat("HH:mm:ss", locale)

    // Test-Daten generieren
    val baseCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    val testZeiten = (40..59).map { mins ->
        val cal = baseCal.clone() as Calendar
        cal.set(Calendar.MINUTE, mins)
        cal.time
    }

    MathClockTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "timeInWords, granularity = 5, 40-59",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val splitIndex = (testZeiten.size + 1) / 2
            val firstHalf = testZeiten.take(splitIndex)
            val secondHalf = testZeiten.drop(splitIndex)

            Row(modifier = Modifier.fillMaxWidth()) {
                // left column
                Column(modifier = Modifier.weight(1f)) {
                    firstHalf.forEach { date ->
                        TimeTestItem(date, sdf, 5)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // right column
                Column(modifier = Modifier.weight(1f)) {
                    secondHalf.forEach { date ->
                        TimeTestItem(date, sdf, 5)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeTestItem(date: Date, sdf: SimpleDateFormat, granularity: Int = 15) {
    val context = LocalContext.current
    val zeitString = sdf.format(date)
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$zeitString:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = timeInWords(context, date, granularity),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
