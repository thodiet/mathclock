package com.mathclock

import android.content.Context
import java.util.Calendar
import java.util.Date

/**
 * Converts a time into words with fractions of hours.
 */
fun timeInWords(context: Context, date: Date, granularity: Int = 15): String {
    val cal = Calendar.getInstance().apply { time = date }
    val hour = cal.get(Calendar.HOUR) // 0-11
    val displayHour = if (hour == 0) 12 else hour
    val nextHour = if (displayHour == 12) 1 else displayHour + 1
    val minute = cal.get(Calendar.MINUTE)

    val fraction = minute / granularity
    val offset = minute % granularity

    val prefix = context.getString(R.string.time_prefix)
    return if (offset == 0) {
        if (fraction == 0) "$prefix ${hour2word(context, displayHour, true)}"
        else "$prefix ${fractionInText(context, fraction, granularity)} ${hour2word(context, nextHour)}"
    } else {
        val minRes = if (offset == 1 || (granularity - offset) == 1) R.string.time_minute else R.string.time_minutes
        val minText = context.getString(minRes)
        if (offset < (granularity / 2.0)) {
            val hourPostfix : String
            hourPostfix = if (fraction == 0) hour2word(context, displayHour)
            else hour2word(context, nextHour)
            "$prefix ${number2word(context, offset)} $minText ${context.getString(R.string.time_after)}\n${fractionInText(context, fraction, granularity)} $hourPostfix"
        } else {
            "$prefix ${number2word(context, granularity - offset)} $minText ${context.getString(R.string.time_before)}\n${fractionInText(context, fraction + 1, granularity)} ${
                hour2word(context, nextHour)
            }"
        }
    }
}

/**
 * Returns the textual representation of a fraction.
 */
private fun fractionInText(context: Context, fraction: Int, granularity: Int = 15): String {
    val resId = if (granularity == 15) {
        when (fraction) {
            1 -> R.string.fraction_quarter
            2 -> R.string.fraction_half
            3 -> R.string.fraction_three_quarters
            else -> 0
        }
    } else {
        when (fraction) {
            1 -> R.string.fraction_twelfth
            2 -> R.string.fraction_sixth
            3 -> R.string.fraction_quarter
            4 -> R.string.fraction_third
            5 -> R.string.fraction_five_twelfths
            6 -> R.string.fraction_half
            7 -> R.string.fraction_seven_twelfths
            8 -> R.string.fraction_two_thirds
            9 -> R.string.fraction_three_quarters
            10 -> R.string.fraction_five_sixths
            11 -> R.string.fraction_eleven_twelfths
            else -> 0
        }
    }
    return if (resId != 0) context.getString(resId) else ""
}

/**
 * Returns the textual representation of a number.
 */
private fun number2word(context: Context, number: Int): String {
    val resId = when (number) {
        1 -> R.string.num_1
        2 -> R.string.num_2
        3 -> R.string.num_3
        4 -> R.string.num_4
        5 -> R.string.num_5
        6 -> R.string.num_6
        7 -> R.string.num_7
        else -> 0
    }
    return if (resId != 0) context.getString(resId) else ""
}

/**
 * Returns the textual representation of an hour.
 */
private fun hour2word(context: Context, number: Int, extended: Boolean = false): String {
    val resId = when (number) {
        1 -> if (extended) R.string.hour_1_masc else R.string.hour_1
        2 -> R.string.hour_2
        3 -> R.string.hour_3
        4 -> R.string.hour_4
        5 -> R.string.hour_5
        6 -> R.string.hour_6
        7 -> R.string.hour_7
        8 -> R.string.hour_8
        9 -> R.string.hour_9
        10 -> R.string.hour_10
        11 -> R.string.hour_11
        12 -> R.string.hour_12
        else -> 0
    }
    val hour = if (resId != 0) context.getString(resId) else ""
    return if (extended) "$hour ${context.getString(R.string.time_suffix_o_clock)}" else hour
}
