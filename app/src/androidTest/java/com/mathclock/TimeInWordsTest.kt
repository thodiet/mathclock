/*
 * Copyright (C) 2026 thodiet <thodiet@protonmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mathclock

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.Date

@RunWith(AndroidJUnit4::class)
class TimeInWordsTest {

    private val baseContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val styles = listOf("de", "mth", "swg")

    private fun getContext(style: String): Context = MathClockWidget.getLocalizedContext(baseContext, style)

    @Test
    fun test_0() {
        styles.forEach { style ->
            val context = getContext(style)
            val cal = Calendar.getInstance()
            cal.set(Calendar.MINUTE, 0)
            val date: Date = cal.time

            // Check 15-minute granularity
            val result15 = timeInWords(context, date, 15)
            val oClockSuffix = context.getString(R.string.time_suffix_o_clock)

            if (oClockSuffix.isNotEmpty()) {
                assertTrue(
                    "$style - 0 (15min): Expected '$oClockSuffix' in the output, but was: $result15",
                    result15.contains(oClockSuffix)
                )
            }
            assertTrue(
                "$style - 0 (15min): Expected no '${context.getString(R.string.time_before)}' in the output, but was: $result15",
                !result15.contains(context.getString(R.string.time_before))
            )
            assertTrue(
                "$style - 0 (15min): Expected no '${context.getString(R.string.time_after)}' in the output, but was: $result15",
                !result15.contains(context.getString(R.string.time_after))
            )

            // Check 5-minute granularity
            val result5 = timeInWords(context, date, 5)
            if (oClockSuffix.isNotEmpty()) {
                assertTrue(
                    "$style - 0 (5min): Expected '$oClockSuffix' in the output, but was: $result5",
                    result5.contains(oClockSuffix)
                )
            }
            assertTrue(
                "$style - 0 (5min): Expected no '${context.getString(R.string.time_before)}' in the output, but was: $result5",
                !result5.contains(context.getString(R.string.time_before))
            )
            assertTrue(
                "$style - 0 (5min): Expected no '${context.getString(R.string.time_after)}' in the output, but was: $result5",
                !result5.contains(context.getString(R.string.time_after))
            )
        }
    }

    @Test
    fun test_vor() {
        styles.forEach { style ->
            val context = getContext(style)
            val cal = Calendar.getInstance()
            val minutes = (8..14) + (23..29) + (38..44) + (53..59)
            val beforeWord = context.getString(R.string.time_before)
            val oClockSuffix = context.getString(R.string.time_suffix_o_clock)

            minutes.forEach {
                cal.set(Calendar.MINUTE, it)
                val result = timeInWords(context, cal.time)

                assertTrue(
                    "$style - $it: Expected '$beforeWord' in the output, but was: $result",
                    result.contains(beforeWord)
                )
                if (oClockSuffix.isNotEmpty()) {
                    assertTrue(
                        "$style - $it: Expected no '$oClockSuffix' in the output, but was: $result",
                        !result.contains(oClockSuffix)
                    )
                }
            }
        }
    }

    @Test
    fun test_nach() {
        styles.forEach { style ->
            val context = getContext(style)
            val cal = Calendar.getInstance()
            val minutes = (1..7) + (16..22) + (31..37) + (46..52)
            val afterWord = context.getString(R.string.time_after)
            val oClockSuffix = context.getString(R.string.time_suffix_o_clock)

            minutes.forEach {
                cal.set(Calendar.MINUTE, it)
                val result = timeInWords(context, cal.time)

                assertTrue(
                    "$style - $it: Expected '$afterWord' in the output, but was: $result",
                    result.contains(afterWord)
                )
                if (oClockSuffix.isNotEmpty()) {
                    assertTrue(
                        "$style - $it: Expected no '$oClockSuffix' in the output, but was: $result",
                        !result.contains(oClockSuffix)
                    )
                }
            }
        }
    }

    @Test
    fun test_Viertel() {
        styles.forEach { style ->
            val context = getContext(style)
            val cal = Calendar.getInstance()
            val minutes = (8..22)
            val quarterWord = context.getString(R.string.fraction_quarter)
            val oClockSuffix = context.getString(R.string.time_suffix_o_clock)

            minutes.forEach {
                cal.set(Calendar.MINUTE, it)
                val result = timeInWords(context, cal.time)

                assertTrue(
                    "$style - $it: Expected '$quarterWord' in the output, but was: $result",
                    result.contains(quarterWord)
                )
                if (oClockSuffix.isNotEmpty()) {
                    assertTrue(
                        "$style - $it: Expected no '$oClockSuffix' in the output, but was: $result",
                        !result.contains(oClockSuffix)
                    )
                }
            }
        }
    }

    @Test
    fun test_Halb() {
        styles.forEach { style ->
            val context = getContext(style)
            val cal = Calendar.getInstance()
            val minutes = (23..37)
            val halfWord = context.getString(R.string.fraction_half)
            val oClockSuffix = context.getString(R.string.time_suffix_o_clock)

            minutes.forEach {
                cal.set(Calendar.MINUTE, it)
                val result = timeInWords(context, cal.time)

                assertTrue(
                    "$style - $it: Expected '$halfWord' in the output, but was: $result",
                    result.contains(halfWord)
                )
                if (oClockSuffix.isNotEmpty()) {
                    assertTrue(
                        "$style - $it: Expected no '$oClockSuffix' in the output, but was: $result",
                        !result.contains(oClockSuffix)
                    )
                }
            }
        }
    }

    @Test
    fun test_Dreiviertel() {
        styles.forEach { style ->
            val context = getContext(style)
            val cal = Calendar.getInstance()
            val minutes = (38..52)
            val threeQuartersWord = context.getString(R.string.fraction_three_quarters)
            val oClockSuffix = context.getString(R.string.time_suffix_o_clock)

            minutes.forEach {
                cal.set(Calendar.MINUTE, it)
                val result = timeInWords(context, cal.time)

                assertTrue(
                    "$style - $it: Expected '$threeQuartersWord' in the output, but was: $result",
                    result.contains(threeQuartersWord)
                )
                if (oClockSuffix.isNotEmpty()) {
                    assertTrue(
                        "$style - $it: Expected no '$oClockSuffix' in the output, but was: $result",
                        !result.contains(oClockSuffix)
                    )
                }
            }
        }
    }

    @Test
    fun testNeitherNor_vor_nach() {
        styles.forEach { style ->
            val context = getContext(style)
            val cal = Calendar.getInstance()
            val oClockSuffix = context.getString(R.string.time_suffix_o_clock)
            // 15-Minuten-Granularität
            val minutes15 = arrayOf(15, 30, 45)
            minutes15.forEach { minute ->
                cal.set(Calendar.MINUTE, minute)
                val result = timeInWords(context, cal.time, 15)
                assertTrue(
                    "$style - $minute (15min): Expected no '${context.getString(R.string.time_before)}'",
                    !result.contains(context.getString(R.string.time_before))
                )
                assertTrue(
                    "$style - $minute (15min): Expected no '${context.getString(R.string.time_after)}'",
                    !result.contains(context.getString(R.string.time_after))
                )
                if (oClockSuffix.isNotEmpty()) {
                    assertTrue(
                        "$style - $minute (15min): Expected no '$oClockSuffix'",
                        !result.contains(oClockSuffix)
                    )
                }
            }

            // 5-Minuten-Granularität
            val minutes5 = arrayOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)
            minutes5.forEach { minute ->
                cal.set(Calendar.MINUTE, minute)
                val result = timeInWords(context, cal.time, 5)
                assertTrue(
                    "$style - $minute (5min): Expected no '${context.getString(R.string.time_before)}'",
                    !result.contains(context.getString(R.string.time_before))
                )
                assertTrue(
                    "$style - $minute (5min): Expected no '${context.getString(R.string.time_after)}'",
                    !result.contains(context.getString(R.string.time_after))
                )
                if (oClockSuffix.isNotEmpty()) {
                    assertTrue(
                        "$style - $minute (5min): Expected no '$oClockSuffix'",
                        !result.contains(oClockSuffix)
                    )
                }
            }
        }
    }

    @Test
    fun test_Fractions_5min() {
        styles.forEach { style ->
            val context = getContext(style)
            val cal = Calendar.getInstance()
            val oClockSuffix = context.getString(R.string.time_suffix_o_clock)
            
            val testData = listOf(
                (3..7) to R.string.fraction_twelfth,
                (8..12) to R.string.fraction_sixth,
                (13..17) to R.string.fraction_quarter,
                (18..22) to R.string.fraction_third,
                (23..27) to R.string.fraction_five_twelfths,
                (28..32) to R.string.fraction_half,
                (33..37) to R.string.fraction_seven_twelfths,
                (38..42) to R.string.fraction_two_thirds,
                (43..47) to R.string.fraction_three_quarters,
                (48..52) to R.string.fraction_five_sixths,
                (53..57) to R.string.fraction_eleven_twelfths
            )

            testData.forEach { (range, resId) ->
                val expectedWord = context.getString(resId)
                range.forEach { minute ->
                    cal.set(Calendar.MINUTE, minute)
                    val result = timeInWords(context, cal.time, 5)
                    assertTrue(
                        "$style - $minute: Expected '$expectedWord' in output: $result",
                        result.contains(expectedWord)
                    )
                    if (oClockSuffix.isNotEmpty()) {
                        assertTrue(
                            "$style - $minute: Expected no '$oClockSuffix' in the output, but was: $result",
                            !result.contains(oClockSuffix)
                        )
                    }
                }
            }
        }
    }
}
