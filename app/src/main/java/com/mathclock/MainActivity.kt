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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
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
                var style by remember { mutableStateOf(MathClockWidget.DEFAULT_STYLE) }
                val current = LocalContext.current

                // Load initial style from Glance state
                LaunchedEffect(Unit) {
                    val manager = GlanceAppWidgetManager(current)
                    val glanceIds = manager.getGlanceIds(MathClockWidget::class.java)
                    if (glanceIds.isNotEmpty()) {
                        val state = MathClockWidget().getAppWidgetState<Preferences>(
                            current,
                            glanceIds.first()
                        )
                        style =
                            state[MathClockWidget.StyleKey] ?: MathClockWidget.DEFAULT_STYLE
                    }
                }

                val localizedContext = MathClockWidget.getLocalizedContext(current, style)

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
                                        Screen.Clock -> localizedContext.getString(R.string.settings_title)
                                        Screen.Info -> localizedContext.getString(R.string.info_title)
                                    }
                                )
                            },
                            navigationIcon = {
                                if (currentScreen != Screen.Clock) {
                                    IconButton(onClick = { currentScreen = Screen.Clock }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = localizedContext.getString(R.string.back_content_description)
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (currentScreen == Screen.Clock) {
                                    IconButton(onClick = { currentScreen = Screen.Info }) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = localizedContext.getString(R.string.info_title)
                                        )
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
                            Screen.Clock -> DigitalClock(style) { style = it }
                            Screen.Info -> InfoScreen(style)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalClock(currentStyle: String, onStyleChange: (String) -> Unit) {
    val current = LocalContext.current
    var currentDate by remember { mutableStateOf(Date()) }

    // Initial value, will be updated by LaunchedEffect
    var transparency by remember {
        mutableFloatStateOf(MathClockWidget.INITIAL_TRANSPARENCY)
    }
    var granularity by remember {
        mutableIntStateOf(MathClockWidget.INITIAL_GRANULARITY)
    }
    var currentFont by remember {
        mutableStateOf(MathClockWidget.DEFAULT_FONT)
    }

    var styleExpanded by remember { mutableStateOf(false) }
    var fontExpanded by remember { mutableStateOf(false) }

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

    val localizedContext = MathClockWidget.getLocalizedContext(current, currentStyle)

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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = localizedContext.getString(
                R.string.transparency_label,
                transparency.roundToInt()
            ),
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
            text = localizedContext.getString(R.string.granularity_label),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        val radioOptions = listOf(15, 5)
        val radioLabels = listOf(
            localizedContext.getString(R.string.granularity_15),
            localizedContext.getString(R.string.granularity_5)
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

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Stil:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(60.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            val styleOptions = listOf("de", "mth", "swg")
            val styleLabels = listOf("Deutsch", "Mathematisch", "Schwäbisch")
            val selectedLabel = styleLabels[styleOptions.indexOf(currentStyle).coerceAtLeast(0)]

            ExposedDropdownMenuBox(
                expanded = styleExpanded,
                onExpandedChange = { styleExpanded = !styleExpanded },
                modifier = Modifier.width(200.dp)
            ) {
                TextField(
                    value = selectedLabel,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = styleExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            true
                        )
                        .height(48.dp)
                )

                ExposedDropdownMenu(
                    expanded = styleExpanded,
                    onDismissRequest = { styleExpanded = false }
                ) {
                    styleOptions.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = styleLabels[index],
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onStyleChange(option)
                                MainScope().launch {
                                    MathClockWidget.updateStyle(
                                        current.applicationContext,
                                        option
                                    )
                                }
                                styleExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Schrift:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(60.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            val fontOptions = listOf("sans_serif", "serif", "cursive", "monospace")
            val fontLabels = listOf("Grotesk", "Antiqua", "Kursiv", "Festbreite")
            val selectedFontLabel = fontLabels[fontOptions.indexOf(currentFont).coerceAtLeast(0)]

            ExposedDropdownMenuBox(
                expanded = fontExpanded,
                onExpandedChange = { fontExpanded = !fontExpanded },
                modifier = Modifier.width(200.dp)
            ) {
                TextField(
                    value = selectedFontLabel,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fontExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            true
                        )
                        .height(48.dp)
                )

                ExposedDropdownMenu(
                    expanded = fontExpanded,
                    onDismissRequest = { fontExpanded = false }
                ) {
                    fontOptions.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = fontLabels[index],
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                currentFont = option
                                MainScope().launch {
                                    MathClockWidget.updateFont(
                                        current.applicationContext,
                                        option
                                    )
                                }
                                fontExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatTime(currentDate, localizedContext.resources.configuration.locales[0]),
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = timeInWords(localizedContext, currentDate, granularity),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontFamily = when (currentFont) {
                    "serif" -> FontFamily.Serif
                    "monospace" -> FontFamily.Monospace
                    "cursive" -> FontFamily.Cursive
                    else -> FontFamily.SansSerif
                }
            )
        }
    }
}

private fun formatTime(date: Date, locale: Locale): String {
    val sdf = SimpleDateFormat("HH:mm:ss", locale)
    return sdf.format(date)
}

@Composable
fun InfoScreen(style: String) {
    val current = LocalContext.current
    val localizedContext = MathClockWidget.getLocalizedContext(current, style)
    val infoText = buildAnnotatedString {
        append(localizedContext.getString(R.string.info_jesus_christ))
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
        ) { append(localizedContext.getString(R.string.info_john_14_6)) }
        append(localizedContext.getString(R.string.info_david_pray))
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
        ) { append(localizedContext.getString(R.string.info_psalm_31_16)) }
        append(localizedContext.getString(R.string.info_explanation))
        append(localizedContext.getString(R.string.info_honor_to_jesus))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = localizedContext.getString(R.string.welcome_title),
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
        DigitalClock("de") {}
    }
}

@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    MathClockTheme {
        InfoScreen("de")
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

    val testZeiten = listOf(
        0, 1, 7, 8, 14, 15, 16, 22, 23, 29, 30, 31,
        37, 38, 44, 45, 46, 52, 53, 59
    ).map { mins ->
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
