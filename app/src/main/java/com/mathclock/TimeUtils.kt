package com.mathclock

import java.util.Calendar
import java.util.Date

/**
 * converts a time into words with fractions of hours
 */
fun timeInWords(date: Date): String {
    val cal = Calendar.getInstance().apply { time = date }
    val hour = cal.get(Calendar.HOUR) // 0-11
    val displayHour = if (hour == 0) 12 else hour
    val nextHour = if (displayHour == 12) 1 else displayHour + 1
    val minute = cal.get(Calendar.MINUTE)

    val granularity = 15
    val fraction = minute / granularity
    val offset = minute % granularity

    val retVal = "Es ist"
    return if (offset == 0) {
        if (fraction == 0) "$retVal $displayHour Uhr"
        else "$retVal ${fractionInText(fraction)} $nextHour"
    } else {
        val minText = if (offset == 1 || (granularity - offset) == 1) "Minute" else "Minuten"
        if (offset < (granularity / 2.0))
            "$retVal $offset $minText nach ${fractionInText(fraction)} ${if (fraction == 0) displayHour else nextHour}"
        else
            "$retVal ${granularity - offset} $minText vor ${fractionInText(fraction + 1)} $nextHour"
    }
}

private fun fractionInText(fraction: Int): String {
    return when (fraction) {
        1 -> "Viertel"
        2 -> "Halb"
        3 -> "Drei Viertel"
        else -> ""
    }
}
