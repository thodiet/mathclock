package com.mathclock

import java.util.Calendar
import java.util.Date

/**
 * Converts a time into words with fractions of hours.
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

    val prefix = "Es ist"
    return if (offset == 0) {
        if (fraction == 0) "$prefix ${hour2word(displayHour, true)}"
        else "$prefix ${fractionInText(fraction)} ${hour2word(nextHour)}"
    } else {
        val minText = if (offset == 1 || (granularity - offset) == 1) "Minute" else "Minuten"
        if (offset < (granularity / 2.0)) {
            val hourPostfix : String
            if (fraction == 0) hourPostfix = hour2word(displayHour)
            else hourPostfix = hour2word(nextHour)
            "$prefix ${number2word(offset)} $minText nach\n${fractionInText(fraction)} $hourPostfix"
        } else {
            "$prefix ${number2word(granularity - offset)} $minText vor\n${fractionInText(fraction + 1)} ${
                hour2word(nextHour)
            }"
        }
    }
}

/**
 * Returns the textual representation of a fraction.
 */
private fun fractionInText(fraction: Int): String {
    return when (fraction) {
        1 -> "Viertel"
        2 -> "Halb"
        3 -> "Dreiviertel"
        else -> ""
    }
}

/**
 * Returns the textual representation of a number.
 */
private fun number2word(number: Int): String {
    return when (number) {
        1 -> "eine"
        2 -> "zwei"
        3 -> "drei"
        4 -> "vier"
        5 -> "fünf"
        6 -> "sechs"
        7 -> "sieben"
        else -> ""
    }
}

/**
 * Returns the textual representation of an hour.
 */
private fun hour2word(number: Int, extended: Boolean = false): String {
    val hour = when (number) {
        1 -> if (extended) "ein" else "eins"
        2 -> "zwei"
        3 -> "drei"
        4 -> "vier"
        5 -> "fünf"
        6 -> "sechs"
        7 -> "sieben"
        8 -> "acht"
        9 -> "neun"
        10 -> "zehn"
        11 -> "elf"
        12 -> "zwölf"
        else -> ""
    }
    return if (extended) "$hour Uhr" else hour
}
